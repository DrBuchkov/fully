(ns fully.system.api
  (:require [fully.system.core :as core]))

(def system core/system)

(defn start-system! [] (core/start-system!))

(defn stop-system! [] (core/stop-system!))
