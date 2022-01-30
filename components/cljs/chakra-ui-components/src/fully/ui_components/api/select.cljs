(ns fully.ui-components.api.select
  (:require [fully.ui-components.select :as core]))


(defn Select [{:keys [options] :as props}]
  [core/Select props])
