(ns fully.repository.core-test
  (:require [clojure.test :refer :all]
            [fully.test.system :refer :all]
            [fully.repository.core :as sut]
            [fully.schema.interface :refer [create-schema-manager]]
            [fully.protocols.repository :as r]
            [fully.protocols.schema :as s]
            [com.stuartsierra.component :as component]
            [crux.api :as crux]
            [malli.util :as mu]))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager)
    :repository (component/using
                  (sut/create-repository)
                  [:schema-manager])))

(use-fixtures :each (with-system create-system))

(deftest repository-test
  (testing "Repository CRUD methods"
    (testing "Save method"
      (let [{:keys [repository schema-manager]} *system*
            {:keys [conn]} repository
            user (s/generate schema-manager :example/user)
            [user-id tx] (r/save! repository :example/user user)]
        (testing "should return a generated user id"
          (is (not (nil? user-id))))
        (testing "should return a transaction"
          (is (not (nil? tx))))
        (crux/await-tx conn tx)
        (testing "should add user to db"
          (let [queried-user (crux/entity (crux/db conn) user-id)]
            (is (= queried-user (-> user
                                    (assoc :fully.db/type :example/user)
                                    (assoc :crux.db/id user-id)
                                    (assoc :user/id user-id))))))))))
