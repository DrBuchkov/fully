(ns fully.entity-manager.core
  (:require [fully.logger.api :as log]
            [fully.entity-manager-protocol.api :as em]
            [com.stuartsierra.component :as component]
            [fully.schema-manager-protocol.api :as scm]
            [fully.schema-utils.api :as su]))


(defrecord XtdbEntityManager [schema-manager]
  component/Lifecycle
  (start [this]
    (log/info "XTDB Entity Manager started")
    this)

  (stop [this]
    (log/info "XTDB Entity Manager stopped")
    (assoc this :node nil
                :config nil
                :schema-manager nil
                :transactions nil))

  em/IEntityManager
  (prepare [_ type entity]
    (let [{:keys [fully.entity/id-key]} (scm/properties schema-manager type)
          generated (scm/generate schema-manager type
                                  {:pipe #(su/select-keys % [id-key])})]
      (assoc (merge entity
                    generated)
        :xt/id (get generated id-key)
        :fully.entity/type type))))


(defn create-entity-manager []
  (map->XtdbEntityManager {}))
