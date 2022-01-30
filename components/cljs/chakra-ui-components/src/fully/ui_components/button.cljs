(ns fully.ui-components.button
  (:require ["@chakra-ui/react" :as chakra]))

(defn Button
  ([text]
   [:> chakra/Button text])
  ([props text]
   [:> chakra/Button props text]))