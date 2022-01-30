(ns fully.ui-components.api.text
  (:require [fully.ui-components.text :as core]))


(defn Text
  ([text]
   [core/Text text])
  ([props text]
   [core/Text props text]))
