(ns fully.ui-components.heading
  (:require ["@chakra-ui/react" :as chakra]))


(defn Heading
  ([text]
   [:> chakra/Heading text])
  ([props text]
   [:> chakra/Heading props text]))