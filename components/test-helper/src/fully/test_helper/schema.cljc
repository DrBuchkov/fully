(ns fully.test-helper.schema)

(def schema
  {:example/user   [:map {:fully/entity?   true
                          :fully.entity/id-key :user/id}
                    [:user/id :uuid]
                    [:user/username :string]
                    [:user/password :string]
                    [:user/email :string]
                    [:user/active? :boolean]
                    [:user/premium? :boolean]
                    ;[:user/threads [:* [:ref :example/thread]]]
                    ;[:user/comments [:vector [:ref :example/comment]]]
                    ]

   :example/thread [:map {:fully/entity? true}
                    [:thread/body :string]
                    #_[:thread/comments [:vector [:ref :example/comment]]]
                    ]

   ;:example/comment [:map {:fully/entity? true}
   ;                  [:comment/body :string]
   ;                  [:comment/replies [:vector [:ref :example/comment]]]]
   })
