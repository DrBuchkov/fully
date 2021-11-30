(ns fully.middleware.api
  (:require [fully.middleware.core :as core]))

(defn wrap-middleware [handler]
  (core/wrap-middleware handler))



