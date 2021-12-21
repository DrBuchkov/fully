(ns fully.repository.core
  (:require [fully.config.api :refer [env]]
            [fully.errors.api :as err]
            [fully.logger.api :as log]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [xtdb.api :as xtdb]
            [fully.schema-utils.api :as su]
            [potpuri.core :as pt])
  (:import (xtdb.api IXtdb)))

(defn build-clauses [where]
  (when where
    (reduce (fn [acc [k _]]
              (let [prop-sym (symbol (str "?" (namespace k)) (name k))]
                (-> acc
                    (update :in conj prop-sym)
                    (update :where conj ['?e k prop-sym]))))
            {:in    []
             :where []}
            where)))

(defn build-query [{:keys [limit offset where projection order-by] :as opts}]
  (let [{:keys [in where]} (build-clauses where)]
    (cond-> (assoc '{:in    [?type]
                     :where [[?e :fully.entity/type ?type]]}
              :find [(list 'pull '?e (or projection '[*]))])

      limit (assoc :limit limit)
      offset (assoc :offset offset)
      in (update :in into in)
      where (update :where into where))))

(defrecord XtdbRepository
  [config schema-manager entity-manager transactions ^IXtdb node]

  component/Lifecycle
  (start [this]
    (log/info "Starting connection with XTDB database")
    (let [node (xtdb/start-node config)]
      (log/info "Connection with XTDB database started")
      (assoc this :node node
                  :transactions [])))

  (stop [this]
    (when node
      (log/info "Stopping XTDB Database connection")
      (.close node)
      (log/info "XTDB Database connection stopped")
      (assoc this :node nil
                  :config nil
                  :schema-manager nil
                  :entity-manager nil
                  :transactions nil)))

  repo/IRepository
  (exists? [_ type id]
    (-> (xtdb/q (xtdb/db node)
                '{:find  [?e]
                  :in    [?id ?type]
                  :where [[?e :xt/id ?id]
                          [?e :fully.entity/type ?type]]}
                id type)
        ffirst
        some?))

  (fetch [this type id]
    (repo/fetch this type id nil))

  (fetch [_ type id {:keys [projection]}]

    (ffirst (xtdb/q (xtdb/db node)
                    (assoc '{:in    [?id ?type]
                             :where [[?e :xt/id ?id]
                                     [?e :fully.entity/type ?type]]}
                      :find [(list 'pull '?e (or projection '[*]))])
                    id type)))

  (find [this type]
    (repo/find this type nil))

  (find [_ type {:keys [where] :as opts}]
    (let [query (build-query opts)]
      (-> (partial xtdb/q (xtdb/db node) query type)
          (apply (vals where))
          (seq)
          (flatten))))

  (save [this type entity]
    (when-not (scm/valid? schema-manager type entity)
      (err/validation-error! {:type type
                              :data entity}))
    (update this :transactions conj [::xtdb/put (assoc entity :fully.entity/type type)]))

  (update [this type id entity-data]
    (when-not (scm/valid? schema-manager type entity-data
                          {:pipe su/optional-keys})
      (err/validation-error! (pt/map-of type entity-data)))

    (let [current-resource (repo/fetch this type id)
          updated-resource (merge current-resource entity-data)]
      (update this :transactions conj [::xtdb/put updated-resource])))

  (delete [this type id]
    (update this :transactions conj [::xtdb/delete id]))

  (flush! [this]
    (xtdb/await-tx node (xtdb/submit-tx node transactions))
    (assoc this :transactions []))

  (flush-async! [this]
    (xtdb/submit-tx node transactions)
    (assoc this :transactions [])))

(defn create-repository [config]
  (map->XtdbRepository {:config config}))