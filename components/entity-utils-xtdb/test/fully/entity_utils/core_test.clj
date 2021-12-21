(ns fully.entity-utils.core-test
  (:require [clojure.test :refer :all]
            [fully.test-helper.with-system :refer :all]
            [fully.test-helper.schema :as test-schema]
            [fully.schema-manager.api :refer [create-schema-manager]]
            [com.stuartsierra.component :as component]
            [fully.entity-utils.api :as sut]
            [fully.schema-manager-protocol.api :as scm]
            [fully.schema-utils.api :as su]))
(defn create-system []
  (component/system-map
    :schema-manager (create-schema-manager test-schema/schema)))

(use-fixtures :each (with-system create-system))

(deftest gen-id-test
  (let [{:keys [schema-manager]} *system*
        user (scm/generate schema-manager :example/user {:pipe #(su/dissoc % :user/id)})
        {xt-id   :xt/id
         user-id :user/id
         :as     user-with-id} (sut/gen-id schema-manager :example/user user)]
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
               :user/id user-id))))))
