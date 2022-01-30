(ns fully.ui-components.api.link
  (:require [fully.ui-components.link :as core]))


(defn Link
  ([text]
   [core/Link text])
  ([props text]
   [core/Link props text]))
