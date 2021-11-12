(ns fully.database.core
  (:require [com.stuartsierra.component :as component]
            [crux.api :as crux]
            [fully.logger.interface :as log]
            [fully.errors.interface :as err]
            [fully.protocols.db :as db]
            [fully.protocols.schema :as s]
            [malli.util :as mu]
            [potpuri.core :as pt])
  (:import (crux.api ICruxAPI)
           (java.util UUID)))

(defn resource-exists? [conn id]
  (ffirst (crux/q (crux/db conn)
                  '{:find  [resource]
                    :in    [id]
                    :where [[resource :crux.db/id id]]}
                  id)))

(defrecord CruxConnectionManager [config schema-manager ^ICruxAPI conn]

  component/Lifecycle
  (start [this]
    (log/info "Starting connection with Crux database")
    (let [conn (crux/start-node {})]
      (log/info "Connection with Crux database started")
      (assoc this :conn conn)))

  (stop [this]
    (when conn
      (log/info "Stopping Crux Database connection")
      (.close conn)
      (log/info "Crux Database connection stopped")
      (assoc this :conn nil)))

  ; TODO: Should exceptions be thrown here?
  db/IDatabaseManager
  (create-resource! [_ type resource]
    (when-not (s/valid? schema-manager type resource)
      (err/validation-error! (pt/map-of type resource)))
    (let [id (UUID/randomUUID)]
      [id (crux/submit-tx conn [[:crux.tx/put (-> resource
                                                  (assoc :crux.db/id id)
                                                  (assoc :fully.db/type type))]])]))

  (get-resource! [_ _ id]
    (let [entity (crux/entity (crux/db conn) id)]
      (when-not entity
        (err/not-found! {:id id}))
      entity))

  (list-resources! [_ type]
    (-> (crux/q (crux/db conn)
                '{:find  [(pull ?resource [*])]
                  :in    [?type]
                  :where [[?resource :fully.db/type ?type]]}
                type)
        seq
        flatten))

  (put-resource! [_ type id resource]
    (when-not (resource-exists? conn id)
      (err/not-found! (pt/map-of id)))

    (when-not (s/valid? schema-manager type resource mu/closed-schema)
      (err/validation-error! (pt/map-of type resource)))

    {:id       id
     :resource resource
     :tx       (crux/submit-tx conn [[:crux.tx/put
                                      ; Make sure that :crux.db/id, :fully.db/type are not overridden
                                      (-> resource
                                          (assoc :crux.db/id id)
                                          (assoc :fully.db/type type))]])})

  (patch-resource! [this type id resource]
    (when-not (s/valid? schema-manager type resource
                          (comp mu/closed-schema mu/optional-keys))
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

  (delete-resource! [_ _ id]
    (when-not (resource-exists? conn id)
      (err/not-found! (pt/map-of id)))
    (crux/submit-tx conn [[:crux.tx/delete id]])))

(defn create-db-manager [config]
  (map->CruxConnectionManager {:config config}))


