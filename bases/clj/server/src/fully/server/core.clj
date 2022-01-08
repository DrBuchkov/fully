(ns fully.server.core
  (:require [fully.system.api :as sys]
            [com.stuartsierra.component :as component])
  (:gen-class))

(defn start-app! []
  (component/start-system (sys/create-system)))

(defn stop-app! [system]
  (component/stop-system system))

(defn -main [& args]
  (let [system (start-app!)]
    (.addShutdownHook
      (Runtime/getRuntime)
      (Thread. ^Runnable (fn [] (stop-app! system))))))
