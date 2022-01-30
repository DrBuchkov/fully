(ns fully.ui-components.text
  (:require ["@chakra-ui/react" :as chakra]))


(defn Text
  ([text]
   [:> chakra/Text text])
  ([props text]
   [:> chakra/Text props text]))