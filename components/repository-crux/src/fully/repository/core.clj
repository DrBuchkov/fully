(ns fully.repository.core
  (:require [fully.config.api :refer [env]]
            [fully.errors.api :as err]
            [fully.logger.api :as log]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [crux.api :as crux]
            [malli.util :as mu]
            [potpuri.core :as pt])
  (:import (java.util UUID)
           (crux.api ICruxAPI)))

(defrecord CruxRepository [config schema-manager ^ICruxAPI conn]

  component/Lifecycle
  (start [this]
    (log/info "Starting connection with Crux database")
    (let [conn (crux/start-node config)]
      (log/info "Connection with Crux database started")
      (assoc this :conn conn)))

  (stop [this]
    (when conn
      (log/info "Stopping Crux Database connection")
      (.close conn)
      (log/info "Crux Database connection stopped")
      (assoc this :conn nil)))

  ; TODO: Should exceptions be thrown here?
  repo/IRepository
  (save! [_ type resource]
    (when-not (scm/valid? schema-manager type resource)
      (err/validation-error! (pt/map-of type resource)))
    (let [entity-id-key (scm/entity-id-key schema-manager type)
          id (or (get resource entity-id-key) (UUID/randomUUID))]
      [id (crux/submit-tx
            conn
            [[:crux.tx/put
              (-> resource
                  (assoc entity-id-key id)
                  (assoc :fully.db/type type))]])]))

  (exists? [_ _ id]
    (ffirst (crux/q (crux/db conn)
                    '{:find  [resource]
                      :in    [id]
                      :where [[resource :crux.db/id id]]}
                    id)))
  (fetch! [_ _ id]
    (let [entity (crux/entity (crux/db conn) id)]
      (when-not entity
        (err/not-found! {:id id}))
      entity))

  (find! [_ type]
    (-> (crux/q (crux/db conn)
                '{:find  [(pull ?resource [*])]
                  :in    [?type]
                  :where [[?resource :fully.db/type ?type]]}
                type)
        seq
        flatten))

  (update! [_ type id resource]
    (when-not (scm/valid? schema-manager type resource
                          {:pipe mu/optional-keys})
      (err/validation-error! (pt/map-of type resource)))

    (let [current-resource (crux/entity (crux/db conn) id)
          _ (when-not current-resource
              (err/not-found! (pt/map-of id)))
          updated-resource (merge current-resource resource)]
      {:id       id
       :resource updated-resource
       :tx       (crux/submit-tx conn
                                 [[:crux.tx/put
                                   ; Make sure that :crux.db/id, :fully.db/type are not overridden
                                   (-> updated-resource
                                       (assoc :crux.db/id id)
                                       (assoc :fully.db/type type))]])}))

  (delete! [this type id]
    (when-not (repo/exists? this type id)
      (err/not-found! (pt/map-of id)))
    (crux/submit-tx conn [[:crux.tx/delete id]])))

(defn create-repository []
  (map->CruxRepository {:config (:repository env)}))


