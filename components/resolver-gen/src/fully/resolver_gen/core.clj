(ns fully.resolver-gen.core
  (:require [potpuri.core :as pt]
            [inflections.core :as i]
            [fully.protocols.api :as proto]
            [com.wsscode.pathom3.connect.operation :as pco]
            [malli.core :as m]))


(defn id-field?
  [[k p _]]
  (or (:fully/id? p)
      (= (name k) "id")))


; TODO: this should use fully.schema.api
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
                          (proto/fetch! db schema-name (get input id-key)))}))

     ; fetch all resolver
     (let [op-name (str "all-" (i/plural unqualified-schema-name))
           outkey (keyword (namespace schema-name) (name op-name))]
       ; ex. op-name user => all-users
       (pco/resolver
         {::pco/op-name (symbol op-name)
          ::pco/output  [{outkey [id-key]}]
          ; ex ::pco/output :example/user => {:example/all-users [:user/id :user/username :user/password :user/email]}
          ::pco/resolve (fn [_ _]
                          {outkey (proto/find! db schema-name)})}))]))

(defn schemas->resolvers [db schemas]
  (let [schema->resolvers (partial schema->resolvers db)]
    (->> schemas
         (pt/filter-vals entity-schema?)
         (map schema->resolvers)
         (apply concat))))
