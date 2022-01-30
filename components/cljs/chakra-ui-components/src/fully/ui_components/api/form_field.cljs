(ns fully.ui-components.api.form-field
  (:require [fully.ui-components.form-field :as core]))


(defn FormField [& children]
  (into [core/FormField] children))
