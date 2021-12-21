(ns fully.system.core
  (:require [fully.config.api :refer [env]]
            [com.stuartsierra.component :as component]
            [fully.entity-manager.api :as em]
            [fully.repository.api :as db]
            [fully.resolver.api :as resolver]
            [fully.routes.api :refer [routes]]
            [fully.ring-server.api :as server]
            [fully.schema.api :refer [schema]]
            [fully.schema-manager.api :as scm]))

(defn create-system []
  (let [{:keys [repository]} env]
    (component/system-map
      :schema-manager (scm/create-schema-manager schema)
      :entity-manager (component/using
                        (em/create-entity-manager)
                        [:schema-manager])
      :repository (component/using
                    (db/create-repository repository)
                    [:schema-manager :entity-manager])
      :resolver (component/using
                  (resolver/create-resolver)
                  [:repository :schema-manager])
      :ring-server (server/create-ring-server))))
