(ns fully.schema.core
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [aero.core :as aero]
            [malli.core :as m]
            [malli.generator :as mg]
            [fully.protocols.schema :as s]
            [fully.config.interface :refer [env]]
            [clojure.test.check.generators :as gen]
            [malli.util :as mu]))


(defrecord SchemaManager [config generator schema registry]

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

  s/ISchemaManager
  (properties [_ type]
    (-> type
        (m/schema {:registry registry})
        m/deref
        m/properties))
  (valid? [this type data]
    (s/valid? this type data nil))
  (valid? [_ type data {:keys [pipe]
                        :or   {pipe identity}}]
    (-> (m/schema type {:registry registry})
        pipe
        (m/validate data)))
  (generate [this type]
    (s/generate this type nil))
  (generate [_ type {:keys [pipe]
                     :or   {pipe identity}}]
    (-> (m/schema type {:registry registry})
        pipe
        generator
        gen/generate))
  (sample [this type]
    (s/sample this type nil))
  (sample [_ type {:keys [pipe sample-num]
                   :or   {pipe identity sample-num 10}}]
    (-> (m/schema type {:registry registry})
        pipe
        generator
        (gen/sample sample-num))))

(defn create-schema-manager []
  (map->SchemaManager {:config (:schema env)}))
