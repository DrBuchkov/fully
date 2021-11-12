(ns dev
  (:require [clojure.tools.namespace.repl :as tn]
            [fully.server.core :refer [start-app! stop-app!]]
            [malli.core :as m]
            [malli.util :as mu]
            [malli.generator :as mg]
            [malli.generator :as mg]))

(defonce system (atom nil))

(defn refresh-ns
  "Refresh/reloads all the namespace"
  []
  (tn/refresh))

(defn start!
  "Mount starts life cycle of runtime state"
  []
  (reset! system (start-app!)))

(defn stop!
  "Mount stops life cycle of runtime state"
  []
  (swap! system stop-app!))

(defn restart-dev!
  []
  (stop!)
  ;(refresh-ns)
  (start!))

(comment

  (def registry {:example/user    [:map {:fully/entity? true}
                                   [:user/id :uuid]
                                   [:user/username :string]
                                   [:user/password :string]
                                   [:user/email :string]
                                   ;[:user/threads [:vector [:ref :example/thread]]]
                                   ;[:user/comments [:vector [:ref :example/comment]]]
                                   ]

                 ;:example/thread  [:map {:fully/entity? true}
                 ;                  [:thread/body :string]
                 ;                  [:thread/author :example/user]
                 ;                  [:thread/comments [:vector [:ref :example/comment]]]]

                 ;:example/comment [:map {:fully/entity? true}
                 ;                  [:comment/body :string]
                 ;                  [:comment/replies [:vector [:ref :example/comment]]]]
                 })


  (mg/generate (m/schema :example/user {:registry registry})))


