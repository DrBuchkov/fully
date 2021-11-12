(ns fully.routes.default.core
  (:require [ring.util.http-response :refer [ok]]))

(def routes
  [["/api"
    ["/ping" {:get (constantly (ok {:message "pong"}))}]]])
