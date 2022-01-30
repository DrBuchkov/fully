(ns fully.ui-components.api.radio
  (:require [fully.ui-components.radio :as core]))


(defn Radio [{:keys [options direction] :as props}]
  [core/Radio props])
