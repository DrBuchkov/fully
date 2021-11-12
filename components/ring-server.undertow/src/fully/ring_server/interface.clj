(ns fully.ring-server.interface
  (:require [fully.ring-server.core :as core]))

(defn http-server [config]
  (core/http-server config))
