(ns fully.schema.core
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [aero.core :as aero]
            [malli.core :as m]
            [malli.generator :as mg]
            [fully.schema-manager-protocol.api :as scm]
            [fully.config.api :refer [env]]
            [clojure.test.check.generators :as gen]
            [malli.util :as mu]))


(defrecord SchemaManager [config generator domain-schema registry]

  component/Lifecycle
  (start [this]
    (let [schema (->> (:path config)
                      io/resource
                      aero/read-config)]
      (-> this
          (assoc :schema schema)
          (assoc :registry (merge (m/default-schemas) (mu/schemas) schema))
          (assoc :generator (memoize mg/generator)))))

  (stop [this]
    (-> this
        (assoc :schema nil)
        (assoc :registry nil)
        (assoc :generator nil)))

  scm/ISchemaManager
  (properties [_ type]
    (-> type
        (m/schema {:registry registry})
        m/deref
        m/properties))

  (children [_ type]
    (-> type
        (m/schema {:registry registry})
        m/deref
        m/children))

  (valid? [this type data]
    (scm/valid? this type data nil))

  (valid? [_ type data {:keys [pipe]
                        :or   {pipe identity}}]
    (-> (m/schema type {:registry registry})
        pipe
        (m/validate data)))

  (generate [this type]
    (scm/generate this type nil))

  (generate [_ type {:keys [pipe]
                     :or   {pipe identity}}]
    (let [schema (m/schema type {:registry registry})]
      (-> schema
          pipe
          generator
          gen/generate)))

  (sample [this type]
    (scm/sample this type nil))

  (sample [_ type {:keys [pipe sample-num]
                   :or   {pipe identity sample-num 10}}]
    (-> (m/schema type {:registry registry})
        pipe
        generator
        (gen/sample sample-num))))

(defn create-schema-manager []
  (map->SchemaManager {:config (:schema env)}))
