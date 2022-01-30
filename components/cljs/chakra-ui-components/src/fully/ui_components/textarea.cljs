(ns fully.ui-components.textarea
  (:require ["@chakra-ui/react" :as chakra]))


(defn Textarea [props]
  [:> chakra/Textarea props])