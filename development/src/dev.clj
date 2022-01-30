(ns dev
  (:require [clojure.tools.namespace.repl :as tn]
            [clojure.tools.namespace.reload]
            [fully.system.api :refer [system start-system! stop-system!]]))

(defn refresh-ns
  "Refresh/reloads all the namespace"
  []
  (tn/refresh-all))

(defn start!
  "Mount starts life cycle of runtime state"
  []
  (start-system!))

(defn stop!
  "Mount stops life cycle of runtime state"
  []
  (stop-system!))

(defn schema-manager [] (:schema-manager @system))

(defn entity-manager [] (:entity-manager @system))

(defn repository [] (:repository @system))

(defn resolver [] (:resolver @system))

(defn ring-server [] (:ring-server @system))

(defn restart-dev!
  []
  (stop!)
  (refresh-ns)
  (start!))