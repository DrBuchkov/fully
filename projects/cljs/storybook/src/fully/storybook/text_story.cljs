(ns fully.storybook.text-story
  (:require [reagent.core :as r]
            [fully.storybook.helper :as h]
            [fully.ui-components.api.text :refer [Text]]))

(def ^:export default
  (h/->default {:title     "Button Component"
                :component (r/reactify-component Text)
                :args      {:text "Hello World"}}))

(defn ^:export HelloWorldText [args]
  (let [{:keys [text]} (-> args h/->params)]
    (r/as-element [Text text])))
