(ns fully.system.core
  (:require [fully.config.interface :refer [env]]
            [com.stuartsierra.component :as component]
            [fully.repository.interface :as db]
            [fully.resolver.interface :as resolver]
            [fully.routes.interface :refer [routes]]
            [fully.ring-handler.interface :as handler]
            [fully.ring-server.interface :as server]
            [fully.schema.interface :as schema]))

(defn create-system []
  (component/system-map
    :schema-manager (schema/create-schema-manager)
    :repository (component/using
                  (db/create-repository)
                  [:schema-manager])
    :resolver (component/using
                (resolver/create-resolver)
                [:repository :schema-manager])
    :ring-handler (handler/create-ring-handler routes)
    :ring-server (component/using
                   (server/create-ring-server)
                   [:ring-handler])))
