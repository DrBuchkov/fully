(ns dev
  (:require [clojure.tools.namespace.repl :as tn]
            [clojure.tools.namespace.reload]
            [system :refer :all]
            [fully.server.core :refer [start-app! stop-app!]]))


(defn refresh-ns
  "Refresh/reloads all the namespace"
  []
  (tn/refresh-all))

(defn start!
  "Mount starts life cycle of runtime state"
  []
  (reset! system (start-app!)))

(defn stop!
  "Mount stops life cycle of runtime state"
  []
  (swap! system stop-app!))

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