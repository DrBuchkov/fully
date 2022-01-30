(ns fully.storybook.button-story
  (:require [reagent.core :as r]
            [fully.storybook.helper :as h]
            [fully.ui-components.api.button :refer [Button]]
            [flatland.ordered.map :as ord]))

(def ^:export default
  (h/->default {:title     "Button Component"
                :component (r/reactify-component Button)
                :args      (into (ord/ordered-map :text "Hello World")
                                 h/css-sb-args)
                :argTypes  {:on-click {:action "clicked!"}}}))

(defn ^:export HelloWorldButton [args]
  (let [{:keys [text] :as props} (-> args h/->params)]
    (r/as-element [Button (dissoc props :text) text])))