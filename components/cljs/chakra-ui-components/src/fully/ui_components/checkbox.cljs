(ns fully.ui-components.checkbox
  (:require ["@chakra-ui/react" :as chakra]))


(defn Checkbox
  ([text]
   [:> chakra/Checkbox text])
  ([props text]
   [:> chakra/Checkbox props text]))