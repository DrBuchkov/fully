(ns fully.ui-components.api.alert
  (:require [fully.ui-components.alert :as core]))


(defn Alert
  [{:keys [title description icon] :as props} & children]
  (into [core/Alert props] children))
