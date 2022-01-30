(ns fully.server.core
  (:require [fully.system.api :refer [start-system! stop-system!]])
  (:gen-class))



(defn -main [& args]
  (start-system!)
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread. ^Runnable (fn [] (stop-system!)))))
