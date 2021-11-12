(ns fully.ring-handler.interface
  (:require [fully.ring-handler.core :as core]))

(defn create-ring-handler [routes]
  (core/create-ring-handler routes))
