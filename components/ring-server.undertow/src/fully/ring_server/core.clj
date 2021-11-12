(ns fully.ring-server.core
  (:require [com.stuartsierra.component :as component]
            [fully.middleware.interface :refer [wrap-middleware]]
            [ring.adapter.undertow :refer [run-undertow]]
            [fully.logger.interface :as log])
  (:import (io.undertow Undertow)
           (fully.protocols.ring IRingHandlerProvider)))

(defrecord UndertowHttpServer [config ^IRingHandlerProvider ring-handler ^Undertow server]

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

(defn http-server [config]
  (map->UndertowHttpServer {:config config}))
