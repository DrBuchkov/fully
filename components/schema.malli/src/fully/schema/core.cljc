(ns fully.schema.core
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [aero.core :as aero]
            [malli.core :as m]
            [malli.generator :as mg]
            [fully.protocols.schema :refer [ISchemaManager]]
            [fully.config.interface :refer [config]]))


(defrecord SchemaManager [schema-path schema registry]

  component/Lifecycle
  (start [this]
    (let [schema (->> schema-path
                      io/resource
                      aero/read-config)]
      (-> this
          (assoc :schema schema)
          (assoc :registry (merge (m/default-schemas) schema)))))

  (stop [this]
    (-> this
        (assoc :schema nil)
        (assoc :registry nil)))

  ISchemaManager
  (valid? [_ type data]
    (m/validate (m/schema [:schema {:registry registry} type]) data))
  (valid? [_ type data pipe]
    (m/validate (pipe (m/schema [:schema {:registry registry} type])) data))
  (generate [_ type]
    (mg/generate (m/schema [:schema {:registry registry} type])))
  (generate [_ type pipe]
    (mg/generate (pipe (m/schema [:schema {:registry registry} type])))))

(defn create-schema-manager []
  (map->SchemaManager (select-keys config [:schema-path])))
