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

(defn build-clauses [where]
  (when where
    (reduce (fn [acc [k v]]
              (let [prop-sym (symbol (str "?" (namespace k)) (name k))]
                (-> acc
                    (update :in conj prop-sym)
                    (update :where conj ['?e k prop-sym]))))
            {:in    []
             :where []}
            where)))

(defn build-query [{:keys [limit offset where order-by] :as opts}]
  (let [{:keys [in where]} (build-clauses where)]
    (cond-> '{:find  [(pull ?e [*])]
              :in    [?type]
              :where [[?e :fully.entity/type ?type]]}
      limit (assoc :limit limit)
      offset (assoc :offset offset)
      in (update :in into in)
      where (update :where into where))))

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
  (save! [this type resource]
    (let [[id tx] (repo/save-async! this type resource)]
      (xtdb/await-tx conn tx)
      id))

  (save-async! [_ type resource]
    (let [children (scm/children schema-manager type)
          generated-children (into {}
                                   (for [[child-key child-props child-schema] children
                                         :when (and (:fully.entity.generate/generated child-props)
                                                    (not (get resource child-key)))]
                                     [child-key (scm/generate schema-manager child-schema)]))
          augmented-resource (merge resource generated-children)
          ; generate entities props that are nil and can be automatically generated.
          _ (when-not (scm/valid? schema-manager type augmented-resource)
              (err/validation-error! {:type type
                                      :data resource}))
          ; Check for valid resource after nil children are generated
          {:keys [fully.entity/id-key]} (scm/properties schema-manager type)
          id (get augmented-resource id-key)]
      [id (xtdb/submit-tx
            conn
            [[::xtdb/put
              (-> augmented-resource
                  (assoc :xt/id id)
                  (assoc :fully.entity/type type))]])]))

  (exists? [_ _ id]
    (-> (xtdb/q (xtdb/db conn)
                '{:find  [resource]
                  :in    [id]
                  :where [[resource :xt/id id]]}
                id)
        ffirst some?))

  (fetch! [_ _ id]
    (or (xtdb/entity (xtdb/db conn) id)
        (err/not-found! {:id id})))

  (find! [this type]
    (repo/find! this type nil))

  (find! [_ type {:keys [limit offset where order-by] :as opts}]
    (let [query (build-query opts)]
      (flatten (seq (apply (partial xtdb/q (xtdb/db conn) query type) (vals where))))))

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
                         (assoc :fully.entity/type type))]])}))

  (delete! [this type id]
    (when-not (repo/exists? this type id)
      (err/not-found! (pt/map-of id)))
    (xtdb/submit-tx conn [[::xtdb/delete id]])))

(defn create-repository []
  (map->XtdbRepository {:config (:repository env)}))


