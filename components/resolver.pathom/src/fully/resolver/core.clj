(ns fully.resolver.core
  (:require [fully.logger.interface :as log]
            [fully.protocols.resolver :refer [IResolver]]
            [fully.protocols.repository :refer [fetch! find!]]
            [malli.core :as m]
            [malli.util :as mu]
            [potpuri.core :as pt]
            [inflections.core :as i]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.connect.operation :as pco]
            [com.stuartsierra.component :as component]))

(defn id-field?
  [[k p _]]
  (or (:fully/id? p)
      (= (name k) "id")))

(def entity-schema? (fn [schema]
                      (-> schema
                          m/schema
                          m/properties
                          :fully/entity?)))

(defn schema->resolvers [db [schema-name schema]]
  (let [unqualified-schema-name (name schema-name)
        schema-entries (m/entries schema)
        schema-keys (mapv key schema-entries)
        [id-key _ _] (pt/find-first schema-entries id-field?)]
    [; get by id resolver
     (let [op-name (str unqualified-schema-name "-by-id")]
       ; ex. op-name user => user-by-id]
       (pco/resolver
         {::pco/op-name (symbol op-name)
          ::pco/input   [id-key]
          ::pco/output  schema-keys
          ; ex. ::pco/output :example/user => [:user/id :user/username :user/password :user/email]
          ::pco/resolve (fn [_ input]
                          (fetch! db schema-name (get input id-key)))}))

     ; fetch all resolver
     (let [op-name (str "all-" (i/plural unqualified-schema-name))
           outkey (keyword (namespace schema-name) (name op-name))]
       ; ex. op-name user => all-users
       (pco/resolver
         {::pco/op-name (symbol op-name)
          ::pco/output  [{outkey [id-key]}]
          ; ex ::pco/output :example/user => {:example/all-users [:user/id :user/username :user/password :user/email]}
          ::pco/resolve (fn [_ _]
                          {outkey (find! db schema-name)})}))]))

(defn schemas->resolvers [db schemas]
  (let [schema->resolvers (partial schema->resolvers db)]
    (->> schemas
         (pt/filter-vals entity-schema?)
         (map schema->resolvers)
         (apply concat))))

(defrecord Resolver [schema-manager repository resolvers indexes]
  component/Lifecycle
  (start [this]
    (let [resolvers (schemas->resolvers repository (:schema schema-manager))
          this (-> this
                   (assoc :resolvers resolvers)
                   (assoc :indexes (pci/register resolvers)))]
      (log/info "Resolver started.")
      this))
  (stop [this]
    (log/info "Resolver stopped.")
    (-> this
        (assoc :db nil)
        (assoc :schemas nil)
        (assoc :resolvers nil)
        (assoc :indexes nil)))

  IResolver
  (register-resolver [this op] (update this :indexes pci/register op))
  (resolve! [_ query] (p.eql/process indexes query))
  (resolve! [_ entity query] (p.eql/process indexes entity query)))

(defn create-resolver []
  (map->Resolver {}))
