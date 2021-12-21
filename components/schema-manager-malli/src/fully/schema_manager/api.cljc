(ns fully.schema-manager.api
  (:require [fully.schema-manager.core :as core]))


(defn create-schema-manager [schema]
  (core/create-schema-manager schema))
