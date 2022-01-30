(ns fully.ui-components.api.box
  (:require [fully.ui-components.box :as core]))

(defn Box
  [& children]
  (into [core/Box] children))
