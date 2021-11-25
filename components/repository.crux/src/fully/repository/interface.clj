(ns fully.repository.interface
  (:require [fully.repository.core :as core]))

(defn create-repository []
  (core/create-repository))