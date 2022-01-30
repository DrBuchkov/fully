(ns fully.ui-components.api.heading
  (:require [fully.ui-components.heading :as core]))


(defn Heading
  ([text]
   [core/Heading text])
  ([props text]
   [core/Heading props text]))
