(ns fully.ui-components.alert
  (:require ["@chakra-ui/react" :as chakra]))


(defn Alert
  [{:keys [title description icon] :as props} & children]
  (into [:> chakra/Alert props
         (when icon
           (if (true? icon)
             [:> chakra/AlertIcon]
             [:> chakra/AlertIcon {:as icon}]))
         (when title
           [:> chakra/AlertTitle title])
         (when description
           [:> chakra/AlertDescription description])]
        children))