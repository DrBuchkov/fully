(ns fully.system.core
  (:require [fully.config.interface :refer [config]]
            [com.stuartsierra.component :as component]
            [fully.database.interface :as db]
            [fully.resolver.interface :as resolver]
            [fully.routes.interface :refer [routes]]
            [fully.ring-handler.interface :as handler]
            [fully.ring-server.interface :as server]
            [fully.schema.interface :as schema]))

(defn create-system []
  (let [{:keys [database ring-server]} config]
    (component/system-map
      :schema-manager (schema/create-schema-manager)
      :db-manager (component/using
                    (db/create-db-manager database)
                    [:schema-manager])
      :resolver (component/using
                  (resolver/create-resolver)
                  [:db-manager :schema-manager])
      :ring-handler (handler/create-ring-handler routes)
      :ring-server (component/using
                     (server/http-server ring-server)
                     [:ring-handler]))))
