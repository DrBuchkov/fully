(ns fully.ui-components.input
  (:require ["@chakra-ui/react" :as chakra]))


(defn Input [props]
  [:> chakra/Input props])