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

(defn restart-dev!
  []
  (stop!)
  (refresh-ns)
  (start!))


