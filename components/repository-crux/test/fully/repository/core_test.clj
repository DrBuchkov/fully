(ns fully.repository.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.repository.core :as sut]
            [fully.schema.api :refer [create-schema-manager]]
            [fully.protocols.api :as proto]
            [com.stuartsierra.component :as component]
            [crux.api :as crux]))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager)
    :repository (component/using
                  (sut/create-repository)
                  [:schema-manager])))

(use-fixtures :each (with-system create-system))

(deftest repository-test
  (testing "save! method"
    (let [{:keys [repository schema-manager]} *system*
          {:keys [conn]} repository
          user (proto/generate schema-manager :example/user)
          [user-id tx] (proto/save! repository :example/user user)
          _ (crux/await-tx conn tx)]
      (testing "should return a generated user id"
        (is (not (nil? user-id))))
      (testing "should return a transaction"
        (is (not (nil? tx))))
      (testing "should add user to db"
        (let [queried-user (crux/entity (crux/db conn) user-id)]
          (is (= queried-user
                 (-> user
                     (assoc :fully.db/type :example/user)
                     (assoc :crux.db/id user-id))))))
      (testing "should update existing user in db"
        (let [updated-user (-> (crux/entity (crux/db conn) user-id)
                               (assoc :user/email "johndoe@mail.com"))
              [updated-user-id tx] (proto/save! repository :example/user updated-user)
              _ (crux/await-tx conn tx)
              queried-user (crux/entity (crux/db conn) updated-user-id)]
          (is (= user-id updated-user-id))
          (is (= queried-user updated-user)))))))
