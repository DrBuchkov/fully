(ns fully.ui-components.link
  (:require ["@chakra-ui/react" :as chakra]))


(defn Link
  ([text]
   [:> chakra/Link text])
  ([props text]
   [:> chakra/Link props
    text]))