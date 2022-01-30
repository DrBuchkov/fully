(ns fully.ui-components.api.button
  (:require [fully.ui-components.button :as core]))


(defn Button
  ([text]
   [core/Button text])
  ([props text]
   [core/Button props text]))
