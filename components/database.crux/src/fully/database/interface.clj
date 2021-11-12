(ns fully.database.interface
  (:require [fully.database.core :as core]))

(defn create-db-manager [config]
  (core/create-db-manager config))