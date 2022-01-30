(ns fully.ui-components.alert-dialog
  (:require ["@chakra-ui/react" :as chakra]))


; TODO make this reactive component (re-frame)
(defn AlertDialog
  [{:keys [actions title body] :as props}]
  [:> chakra/AlertDialog props
   [:> chakra/AlertDialogOverlay
    [:> chakra/AlertDialogHeader title]
    [:> chakra/AlertDialogCloseButton]
    [:> chakra/AlertDialogBody body]
    [:> chakra/AlertDialogFooter
     (for [[key {:keys [label on-click]}] actions]
       ^{:key key}
       [:> chakra/Button {:on-click on-click} label])]]])