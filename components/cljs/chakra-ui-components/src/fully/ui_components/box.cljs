(ns fully.ui-components.box
  (:require ["@chakra-ui/react" :as chakra]))

(defn Box [& children]
  (into [:> chakra/Box] children))