(ns fully.repository.api
  (:require [fully.repository.core :as core]))

(defn create-repository []
  (core/create-repository))