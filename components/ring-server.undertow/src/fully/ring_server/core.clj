(ns fully.ring-server.core
  (:require [fully.middleware.interface :refer [wrap-middleware]]
            [fully.config.interface :refer [env]]
            [fully.logger.interface :as log]
            [com.stuartsierra.component :as component]
            [ring.adapter.undertow :refer [run-undertow]])
  (:import (io.undertow Undertow)))

(defrecord UndertowHttpServer [config ring-handler ^Undertow server]

  component/Lifecycle
  (start [this]
    (log/info "Starting Undertow HTTP Server")
    (let [server (run-undertow (wrap-middleware (.getHandler ring-handler))
                               config)]
      (log/info "Started Undertow HTTP Server")
      (assoc this :server server)))

  (stop [this]
    (when server
      (log/info "Shutting down Undertow HTTP Server")
      (.stop ^Undertow server)
      (log/info "Shut Down Undertow HTTP Server shut down")
      (assoc this :server nil))))

(defn create-ring-server []
  (map->UndertowHttpServer {:config (:ring-server env)}))
