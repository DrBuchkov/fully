(ns fully.ui-components.number-input
  (:require ["@chakra-ui/react" :as chakra]))


(defn NumberInput [props]
  [:> chakra/NumberInput props
   [:> chakra/NumberInputField]
   [:> chakra/NumberInputStepper
    [:> chakra/NumberIncrementStepper]
    [:> chakra/NumberDecrementStepper]]])