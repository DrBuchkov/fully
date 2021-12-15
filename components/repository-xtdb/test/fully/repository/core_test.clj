(ns fully.repository.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.repository.core :as sut]
            [fully.schema.api :refer [create-schema-manager]]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [xtdb.api :as xtdb]
            [malli.util :as mu]
            [potpuri.core :as pt])
  (:import (java.util UUID)))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager)
    :repository (component/using
                  (sut/create-repository)
                  [:schema-manager])))

(use-fixtures :each (with-system create-system))

(deftest save-async!--new-user-with-id-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        {:keys [user/id] :as user} (scm/generate schema-manager :example/user)
        [user-id tx] (repo/save-async! repository :example/user user)
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
                   (assoc :fully.entity/type :example/user)
                   (assoc :xt/id user-id))))))))

(deftest save-async!--new-user-without-id-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        user (scm/generate schema-manager :example/user
                           {:pipe #(mu/dissoc % :user/id)})
        ; generate a user but without generating a :user/id since it will be generated
        ; by save! anyways
        [user-id tx] (repo/save-async! repository :example/user user)
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
                   (assoc :fully.entity/type :example/user)
                   (assoc :xt/id user-id))))))))

(deftest save-async!--existing-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        user (scm/generate schema-manager :example/user)
        [user-id tx] (repo/save-async! repository :example/user user)
        _ (xtdb/await-tx conn tx)
        stored-user (xtdb/entity (xtdb/db conn) user-id)
        updated-user (assoc stored-user :user/email "johndoe@mail.com")
        [new-user-id tx] (repo/save-async! repository :example/user updated-user)
        _ (xtdb/await-tx conn tx)
        stored-updated-user (xtdb/entity (xtdb/db conn) new-user-id)]
    (testing "new-user-id should equal user-id"
      (is (= new-user-id user-id)))
    (testing "stored-updated-user should equal stored-user except for updated key"
      (is (= stored-updated-user
             (assoc stored-user :user/email "johndoe@mail.com"))))))

(deftest save-async!--invalid-new-user-test
  (let [{:keys [repository]} *system*
        user {:invalid :user}]
    (testing "should throw ExceptionInfo"
      (is (= (catch-thrown-info (repo/save-async! repository :example/user user))
             {:msg  "Data do not conform with schema"
              :data {:fully.http.error/type :fully.http.error/validation-error
                     :fully.http.error/data {:type :example/user
                                             :data user}}})))))

(deftest save-async!--invalid-existing-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [conn]} repository
        user (scm/generate schema-manager :example/user)
        [user-id tx] (repo/save-async! repository :example/user user)
        _ (xtdb/await-tx conn tx)
        stored-user (xtdb/entity (xtdb/db conn) user-id)
        updated-user (assoc stored-user :user/email 1234)]
    (testing "should throw ExceptionInfo"
      (is (= (catch-thrown-info (repo/save-async! repository :example/user updated-user))
             {:msg  "Data do not conform with schema"
              :data {:fully.http.error/type :fully.http.error/validation-error
                     :fully.http.error/data {:type :example/user
                                             :data updated-user}}})))))

(deftest exists?--when-user-exists-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        user (scm/generate schema-manager :example/user)
        user-id (repo/save! repository :example/user user)]
    (testing "should return true when user does exist in DB"
      (is (= true (repo/exists? repository :example/user user-id))))))

(deftest exists?--when-user-doesnt-exist-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        user (scm/generate schema-manager :example/user)
        _ (repo/save! repository :example/user user)]
    (testing "should return false when user does not exist in DB"
      (is (= false (repo/exists? repository :example/user (UUID/randomUUID)))))))

(deftest fetch!--when-user-exists-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        user (scm/generate schema-manager :example/user)
        user-id (repo/save! repository :example/user user)]
    (testing "should return user along with appended values"
      (is (= (repo/fetch! repository :example/user user-id)
             (assoc user :xt/id user-id :user/id user-id :fully.entity/type :example/user))))))

(deftest fetch!--when-user-doesnt-exist-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        user (scm/generate schema-manager :example/user)
        _ (repo/save! repository :example/user user)
        random-uuid (UUID/randomUUID)]
    (testing "should return user along with appended values"
      (is (= (catch-thrown-info (repo/fetch! repository :example/user random-uuid))
             {:msg  (str "No resource found with id " random-uuid),
              :data #:fully.http.error{:type :fully.http.error/not-found,
                                       :data {:id random-uuid}}})))))

(deftest build-clauses-test
  (testing "when input is nil returns nil"
    (is (nil? (sut/build-clauses nil))))
  (testing "when input is valid a map with :in and :where entries
            should be returned that matches the structure of a datalog query"
    (is (= (sut/build-clauses {:user/active?  true
                               :user/premium? false})
           '{:in    [?user/active? ?user/premium?]
             :where [[?e :user/active? ?user/active?]
                     [?e :user/premium? ?user/premium?]]}))))

(deftest build-query-test
  (testing "with 'limit' and 'offset' options"
    (is (= (sut/build-query
             {:limit  10
              :offset 100})
           '{:find   [(pull ?e [*])]
             :in     [?type]
             :where  [[?e :fully.entity/type ?type]]
             :limit  10
             :offset 100})))
  (testing "with 'where' option"
    (is (= (sut/build-query
             {:where {:user/active?  true
                      :user/premium? false}})
           '{:find  [(pull ?e [*])]
             :in    [?type ?user/active? ?user/premium?]
             :where [[?e :fully.entity/type ?type]
                     [?e :user/active? ?user/active?]
                     [?e :user/premium? ?user/premium?]]}))))


(deftest find!--without-parameters-test
  (let [{:keys [repository schema-manager]} *system*
        users (scm/sample schema-manager :example/user)
        user-ids (doall (for [user users] (repo/save! repository :example/user user)))]
    (testing "should return users along with their appended values"
      (is (= (set (repo/find! repository :example/user))
             (set (map (fn [user user-id]
                         (assoc user :xt/id user-id
                                     :user/id user-id
                                     :fully.entity/type :example/user))
                       users user-ids)))))))

(deftest find!--with-parameters-test
  (let [{:keys [repository schema-manager]} *system*
        users (scm/sample schema-manager :example/user {:size 100})
        _ (doseq [user users] (repo/save! repository :example/user user))]
    (let [limit 5
          offset 3
          where {:user/active?  true
                 :user/premium? false}
          result (repo/find! repository :example/user (pt/map-of limit offset where))]
      (testing "result's size should be equal to limit"
        (= limit (count result)))
      (testing "all users in result should be active and not premium"
        (and (every? :user/active? result)
             (not-any? :user/premium? result))))))


