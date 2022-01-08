(ns fully.routes.core
  (:require [ring.util.http-response :refer [ok]]))

(def routes
  [["/api"
    ["/ping" {:get (constantly (ok {:message "pong"}))}]]])
