(ns fully.ui-components.api.checkbox
  (:require [fully.ui-components.checkbox :as core]))


(defn Checkbox
  ([text]
   [core/Checkbox text])
  ([props text]
   [core/Checkbox props text]))
