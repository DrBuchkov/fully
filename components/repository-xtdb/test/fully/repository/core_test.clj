(ns fully.repository.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.repository.core :as sut]
            [fully.schema.api :refer [create-schema-manager]]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [xtdb.api :as xtdb]
            [malli.util :as mu]))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager)
    :repository (component/using
                  (sut/create-repository)
                  [:schema-manager])))

(use-fixtures :each (with-system create-system))

(deftest save!--new-user-with-id-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        {:keys [user/id] :as user} (scm/generate schema-manager :example/user)
        [user-id tx] (repo/save! repository :example/user user)
        _ (xtdb/await-tx conn tx)
        stored-user (xtdb/entity (xtdb/db conn) user-id)]
    (testing "should return a user id"
      (is (not (nil? user-id))))
    (testing "should return a transaction"
      (is (not (nil? tx))))
    (testing "user-id and id should be equal"
      (is (= id user-id)))
    (testing "should add user to database"
      (let []
        (is (= stored-user
               (-> user
                   (assoc :user/id user-id)
                   (assoc :fully.db/type :example/user)
                   (assoc :xt/id user-id))))))))

(deftest save!--new-user-without-id-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        user (scm/generate schema-manager :example/user
                           {:pipe #(mu/dissoc % :user/id)})
        ; generate a user but without generating a :user/id since it will be generated
        ; by save! anyways
        [user-id tx] (repo/save! repository :example/user user)
        _ (xtdb/await-tx conn tx)
        stored-user (xtdb/entity (xtdb/db conn) user-id)]
    (testing "should return a generated user id"
      (is (not (nil? user-id))))
    (testing "should return a transaction"
      (is (not (nil? tx))))
    (testing "should add user to db"
      (let []
        (is (= stored-user
               (-> user
                   (assoc :user/id user-id)
                   (assoc :fully.db/type :example/user)
                   (assoc :xt/id user-id))))))))

(deftest save!--existing-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        user (scm/generate schema-manager :example/user)
        [user-id tx] (repo/save! repository :example/user user)
        _ (xtdb/await-tx conn tx)
        stored-user (xtdb/entity (xtdb/db conn) user-id)
        updated-user (assoc stored-user :user/email "johndoe@mail.com")
        [new-user-id tx] (repo/save! repository :example/user updated-user)
        _ (xtdb/await-tx conn tx)
        stored-updated-user (xtdb/entity (xtdb/db conn) new-user-id)]
    (testing "new-user-id should equal user-id"
      (is (= new-user-id user-id)))
    (testing "stored-updated-user should equal stored-user except for updated key"
      (is (= stored-updated-user
             (assoc stored-user :user/email "johndoe@mail.com"))))))

(deftest save!--invalid-user-test
  (let [{:keys [repository]} *system*
        user {:invalid :user}]
    (is (= (catch-thrown-info (repo/save! repository :example/user user))
           {:msg  "Data do not conform with schema"
            :data {:fully.http.error/type :fully.http.error/validation-error
                   :fully.http.error/data {:type :example/user
                                           :data user}}}))))