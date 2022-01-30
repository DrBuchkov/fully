(ns fully.ui-components.radio
  (:require ["@chakra-ui/react" :as chakra]))


(defn Radio [{:keys [options direction] :as props}]
  [:> chakra/RadioGroup (dissoc props :options :direction)
   [:> chakra/Stack {:direction direction}
    (for [[key {:keys [value label]}] options]
      ^{:key key}
      [:> chakra/Radio {:value value} label])]])