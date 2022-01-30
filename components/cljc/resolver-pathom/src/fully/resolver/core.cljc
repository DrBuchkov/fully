(ns fully.resolver.core
  (:require [fully.logger.api :as log]
            [fully.resolver-protocol.api :as res]
            [potpuri.core :as pt]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.stuartsierra.component :as component]))

(defrecord Resolver [schema-manager repository resolvers indexes]

  component/Lifecycle
  (start [this]
    (log/info "Resolver started.")
    this)

  (stop [this]
    (log/info "Resolver stopped.")
    (-> this
        (assoc :schema-manager nil)
        (assoc :repository nil)
        (assoc :resolvers nil)
        (assoc :indexes nil)))

  res/IResolver
  (register [this op]
    (-> this
        (update :indexes pci/register op)
        (update :resolvers pt/conjv op)))

  (resolve! [_ query] (p.eql/process indexes query))

  (resolve! [_ entity query] (p.eql/process indexes entity query)))

(defn create-resolver []
  (map->Resolver {}))
