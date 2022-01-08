(ns fully.schema-manager.core
  (:require [com.stuartsierra.component :as component]
            [malli.core :as m]
            [malli.generator :as mg]
            [fully.schema-manager-protocol.api :as scm]
            [fully.config.api :refer [env]]
            [clojure.test.check.generators :as gen]
            [malli.util :as mu]))


(defrecord SchemaManager [config generator domain-schema registry]

  component/Lifecycle
  (start [this]
    (-> this
        (assoc :domain-schema domain-schema)
        (assoc :registry (merge (m/default-schemas) (mu/schemas) domain-schema))
        (assoc :generator (memoize mg/generator))))

  (stop [this]
    (-> this
        (assoc :domain-schema nil)
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

  (sample [_ type {:keys [pipe size]
                   :or   {pipe identity size 10}}]
    (-> (m/schema type {:registry registry})
        pipe
        generator
        (gen/sample size))))

(defn create-schema-manager [schema]
  (map->SchemaManager {:config        (:schema env)
                       :domain-schema schema}))
