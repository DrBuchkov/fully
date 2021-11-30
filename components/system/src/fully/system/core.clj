(ns fully.system.core
  (:require [fully.config.api :refer [env]]
            [com.stuartsierra.component :as component]
            [fully.repository.api :as db]
            [fully.resolver.api :as resolver]
            [fully.routes.api :refer [routes]]
            [fully.ring-handler.api :as handler]
            [fully.ring-server.api :as server]
            [fully.schema.api :as schema]))

(defn create-system []
  (component/system-map
    :schema-manager (schema/create-schema-manager)
    :repository (component/using
                  (db/create-repository)
                  [:schema-manager])
    :resolver (component/using
                (resolver/create-resolver)
                [:repository :schema-manager])
    :ring-server (server/create-ring-server)))
