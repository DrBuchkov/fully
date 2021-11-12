(ns fully.middleware.interface
  (:require [fully.middleware.core :as core]))

(defn wrap-middleware [handler]
  (core/wrap-middleware handler))



