(ns fully.ui-components.tag
  (:require ["@chakra-ui/react" :as chakra]))


(defn Tag
  ([text] [Tag {} text])
  ([{:keys [on-delete icon icon-position] :as props} text]
   [:> chakra/Tag (dissoc props :on-delete :icon :icon-position)

    (when icon
      [:> (case icon-position
            :left chakra/TagLeftIcon
            :right chakra/TagRightIcon
            chakra/TagLeftIcon)
       {:as icon}])

    [:> chakra/TagLabel text]

    (when on-delete
      [:> chakra/TagCloseButton {:on-click on-delete}])]))