(ns fully.ui-components.form-field
  (:require ["@chakra-ui/react" :as chakra]))

(defn FormField [& children]
  (into [:> chakra/FormControl] children))