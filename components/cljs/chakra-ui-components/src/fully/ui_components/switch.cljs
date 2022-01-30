(ns fully.ui-components.switch
  (:require ["@chakra-ui/react" :as chakra]))


(defn Switch [props]
  [:> chakra/Switch props])