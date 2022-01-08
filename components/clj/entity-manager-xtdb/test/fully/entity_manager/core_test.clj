(ns fully.entity-manager.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.test-helper.schema :as test-schema]
            [fully.entity-manager-protocol.api :as em]
            [fully.schema-manager-protocol.api :as scm]
            [fully.schema-manager.api :refer [create-schema-manager]]
            [com.stuartsierra.component :as component]
            [fully.schema-utils.api :as su]
            [fully.entity-manager.core :as sut]))


(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager test-schema/schema)
    :entity-manager (component/using
                      (sut/create-entity-manager)
                      [:schema-manager])))

(use-fixtures :each (with-system create-system))

(deftest prepare-test
  (let [{:keys [schema-manager entity-manager]} *system*

        user (scm/generate schema-manager :example/user {:pipe #(su/dissoc % :user/id)})

        {xt-id   :xt/id
         user-id :user/id
         :as     user-with-id}
        (em/prepare entity-manager :example/user user)]
    (testing "generated xt/id is uuid"
      (is (uuid? xt-id)))
    (testing "generated user/id is uuid"
      (is (uuid? user-id)))
    (testing "generated user/id and xt/id are equal"
      (is (= xt-id user-id)))
    (testing "user-with-id should equal user with xt/id and user/id associated"
      (is (= user-with-id
             (assoc user
               :xt/id xt-id
               :user/id user-id
               :fully.entity/type :example/user))))))
