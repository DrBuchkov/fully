(ns fully.ring-server.core
  (:require [fully.middleware.api :refer [wrap-middleware]]
            [fully.ring-handler.api :refer [ring-handler]]
            [fully.config.api :refer [env]]
            [fully.logger.api :as log]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]])
  (:import (org.eclipse.jetty.server Server)))

(defrecord JettyHttpServer [config ^Server server]

  component/Lifecycle
  (start [this]
    (log/info "Starting Jetty HTTP Server")
    (let [server (run-jetty (wrap-middleware ring-handler)
                            config)]
      (log/info "Started Jetty HTTP Server")
      (assoc this :server server)))

  (stop [this]
    (when server
      (log/info "Shutting down Jetty HTTP Server")
      (.stop server)
      (log/info "Shut Down Jetty HTTP Server shut down")
      (assoc this :server nil))))

(defn create-ring-server []
  (map->JettyHttpServer {:config (:ring-server env)}))
