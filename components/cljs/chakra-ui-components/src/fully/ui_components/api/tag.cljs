(ns fully.ui-components.api.tag
  (:require [fully.ui-components.tag :as core]))


(defn Tag
  ([text]
   [core/Tag text])
  ([props text]
   [core/Tag props text]))
