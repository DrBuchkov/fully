(ns fully.repository.core
  (:require [fully.config.api :refer [env]]
            [fully.errors.api :as err]
            [fully.logger.api :as log]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [xtdb.api :as xtdb]
            [malli.util :as mu]
            [potpuri.core :as pt])
  (:import (xtdb.api IXtdb)))

(defrecord XtdbRepository [config schema-manager ^IXtdb conn]

  component/Lifecycle
  (start [this]
    (log/info "Starting connection with XTDB database")
    (let [conn (xtdb/start-node config)]
      (log/info "Connection with XTDB database started")
      (assoc this :conn conn)))

  (stop [this]
    (when conn
      (log/info "Stopping XTDB Database connection")
      (.close conn)
      (log/info "XTDB Database connection stopped")
      (assoc this :conn nil)))

  repo/IRepository
  (save! [_ type resource]
    (let [children (scm/children schema-manager type)
          _ (clojure.pprint/pprint children)
          generated-children (into {}
                                   (for [[child-key child-props child-schema] children
                                         :when (and (:fully.entity.generate/generated child-props)
                                                    (not (get resource child-key)))]
                                     [child-key (scm/generate schema-manager child-schema)]))
          _ (clojure.pprint/pprint generated-children)
          resource (merge resource generated-children)
          ; generate entities props that are nil and can be automatically generated.
          _ (when-not (scm/valid? schema-manager type resource)
              (err/validation-error! (pt/map-of type resource)))
          ; Check for valid resource after nil children are generated
          {:keys [fully.entity/id-key]} (scm/properties schema-manager type)
          id (get resource id-key)
          _ (println "id: " id)]
      [id (xtdb/submit-tx
            conn
            [[::xtdb/put
              (-> resource
                  (assoc :xt/id id)
                  (assoc :fully.db/type type))]])]))

  (exists? [_ _ id]
    (ffirst (xtdb/q (xtdb/db conn)
                    '{:find  [resource]
                      :in    [id]
                      :where [[resource :xt/id id]]}
                    id)))
  (fetch! [_ _ id]
    (or (xtdb/entity (xtdb/db conn) id)
        (err/not-found! {:id id})))

  (find! [_ type]
    (-> (xtdb/q (xtdb/db conn)
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

    (let [current-resource (xtdb/entity (xtdb/db conn) id)
          _ (when-not current-resource
              (err/not-found! (pt/map-of id)))
          updated-resource (merge current-resource resource)]
      {:id       id
       :resource updated-resource
       :tx       (xtdb/submit-tx
                   conn
                   [[::xtdb/put
                     ; Make sure that :xt/id, :fully.db/type are not overridden
                     (-> updated-resource
                         (assoc :xt/id id)
                         (assoc :fully.db/type type))]])}))

  (delete! [this type id]
    (when-not (repo/exists? this type id)
      (err/not-found! (pt/map-of id)))
    (xtdb/submit-tx conn [[:XTDB.tx/delete id]])))

(defn create-repository []
  (map->XtdbRepository {:config (:repository env)}))


