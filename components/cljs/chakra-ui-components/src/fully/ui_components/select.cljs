(ns fully.ui-components.select
  (:require ["@chakra-ui/react" :as chakra]))


(defn Select [{:keys [options] :as props}]
  [:> chakra/Select props
   (for [[key {:keys [value label]}] options]
     ^{:key key}
     [:option {:value value} label])])