(ns fully.repository.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.test-helper.schema :as test-schema]
            [fully.repository.core :as sut]
            [fully.config.api :refer [env]]
            [fully.entity-utils.api :as eu]
            [fully.schema-manager.api :refer [create-schema-manager]]
            [fully.repository-protocol.api :as repo]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]
            [potpuri.core :as pt])
  (:import (java.util UUID)))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn create-system []
  (let [{:keys [repository]} env]
    (component/system-map
      :schema-manager (create-schema-manager test-schema/schema)
      :repository (component/using
                    (sut/create-repository repository)
                    [:schema-manager]))))

(use-fixtures :each (with-system create-system))

(deftest exists?--when-user-exists-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))]
    (testing "should return true when user does exist in DB"
      (is (= true (repo/exists? repository :example/user id))))))

(deftest exists?--when-user-doesnt-exist-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))]
    (testing "should return false when user does not exist in DB"
      (is (= false (repo/exists? repository :example/user (UUID/randomUUID)))))))

(deftest fetch--when-user-exists-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))]
    (testing "should return user along with appended values"
      (is (= (repo/fetch repository :example/user id)
             (assoc user :fully.entity/type :example/user))))))

(deftest fetch--when-user-doesnt-exist-in-db-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))
        random-uuid (UUID/randomUUID)]
    (testing "should return user along with appended values"
      (is (nil? (repo/fetch repository :example/user random-uuid))))))

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


(deftest find--without-parameters-test
  (let [{:keys [repository schema-manager]} *system*
        users (map (partial eu/gen-id schema-manager :example/user) (scm/sample schema-manager :example/user))]
    (repo/flush! (reduce (fn [acc user] (repo/save acc :example/user user)) repository users))
    (testing "should return users along with their appended values"
      (is (= (set (repo/find repository :example/user))
             (set (map #(assoc % :fully.entity/type :example/user) users)))))))

(deftest find--with-parameters-test
  (let [{:keys [repository schema-manager]} *system*
        users (map (partial eu/gen-id schema-manager :example/user) (scm/sample schema-manager :example/user))]
    (repo/flush! (reduce (fn [acc user] (repo/save acc :example/user user)) repository users))
    (let [limit 5
          offset 3
          where {:user/active?  true
                 :user/premium? false}
          result (repo/find repository :example/user (pt/map-of limit offset where))]
      (testing "result's size should be equal to limit"
        (= limit (count result)))
      (testing "all users in result should be active and not premium"
        (and (every? :user/active? result)
             (not-any? :user/premium? result))))))

(deftest save--new-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        repository (repo/save repository :example/user user)]
    (testing "should add the proper transaction"
      (is (= (:transactions repository)
             [[:xtdb.api/put (assoc user :fully.entity/type :example/user)]])))
    (let [repository (repo/flush! repository)
          stored-user (repo/fetch repository :example/user id)]
      (testing "should add user to database"
        (is (= stored-user
               (assoc user :fully.entity/type :example/user)))))))

(deftest save--existing-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))
        stored-user (repo/fetch repository :example/user id)
        updated-user (assoc stored-user :user/email "johndoe@mail.com")
        _ (-> repository
              (repo/save :example/user updated-user)
              (repo/flush!))
        stored-updated-user (repo/fetch repository :example/user id)]
    (testing "stored-updated-user should equal stored-user except for updated key"
      (is (= stored-updated-user
             (assoc stored-user :user/email "johndoe@mail.com"))))))

(deftest save--invalid-new-user-test
  (let [{:keys [repository]} *system*
        user {:invalid :user}]
    (testing "should throw ExceptionInfo"
      (is (= (catch-thrown-info (repo/save repository :example/user user))
             {:msg  "Data do not conform with schema"
              :data {:fully.http.error/type :fully.http.error/validation-error
                     :fully.http.error/data {:type :example/user
                                             :data user}}})))))

(deftest save--invalid-existing-user-test
  (let [{:keys [repository schema-manager]} *system*
        {:keys [user/id] :as user} (eu/gen-id schema-manager :example/user
                                              (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))
        stored-user (repo/fetch repository :example/user id)
        updated-user (assoc stored-user :user/email 1234)]
    (testing "should throw ExceptionInfo"
      (is (= (catch-thrown-info (-> repository
                                    (repo/save :example/user updated-user)
                                    (repo/flush!)))
             {:msg  "Data do not conform with schema"
              :data {:fully.http.error/type :fully.http.error/validation-error
                     :fully.http.error/data {:type :example/user
                                             :data updated-user}}})))))

(deftest update--when-user-exists-in-db-test
  (let [{:keys [repository schema-manager]} *system*

        {:keys [user/id] :as user}
        (eu/gen-id schema-manager :example/user
                   (scm/generate schema-manager :example/user))

        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))

        stored-user (repo/fetch repository :example/user id)
        repository (-> repository
                       (repo/update :example/user id {:user/email "johndoe@mail.com"}))]
    (testing "should add the proper transaction"
      (is (= (:transactions repository)
             [[:xtdb.api/put (assoc stored-user :user/email "johndoe@mail.com")]])))
    (let [repository (repo/flush! repository)
          updated-stored-user (repo/fetch repository :example/user id)]
      (testing "user in database should be updated"
        (is (= (assoc stored-user :user/email "johndoe@mail.com")
               updated-stored-user))))))

(deftest delete-test
  (let [{:keys [repository schema-manager]} *system*

        {:keys [user/id] :as user}
        (eu/gen-id schema-manager :example/user
                   (scm/generate schema-manager :example/user))
        _ (-> repository
              (repo/save :example/user user)
              (repo/flush!))
        repository (repo/delete repository :example/user id)]
    (testing "should add the proper transaction"
      (is (= (:transactions repository)
             [[:xtdb.api/delete id]])))
    (let [repository (repo/flush! repository)]
      (testing "should remove user from database"
        (is (false? (repo/exists? repository :example/user id)))))))