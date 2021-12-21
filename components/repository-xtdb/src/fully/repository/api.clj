(ns fully.repository.api
  (:require [fully.repository.core :as core]))

(defn create-repository [repository]
  (core/create-repository repository))