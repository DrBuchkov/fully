(ns fully.schema-manager.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [fully.test-helper.with-system :refer :all]
            [fully.test-helper.schema :as test-schema]
            [fully.schema-manager.core :as sut]
            [fully.schema-manager-protocol.api :as scm]
            [com.stuartsierra.component :as component]))

(defn create-system []
  (component/system-map
    :schema-manager (sut/create-schema-manager test-schema/schema)))

(use-fixtures :each (with-system create-system))

(deftest schema-manager-test
  (testing "Generated value is valid"
    (let [{:keys [schema-manager]} *system*
          value (scm/generate schema-manager :example/user)]
      (is (= true (scm/valid? schema-manager :example/user value)))))
  (testing "Generated sample is valid"
    (let [{:keys [schema-manager]} *system*
          sample (scm/sample schema-manager :example/user)]
      (doseq [s sample]
        (is (= true (scm/valid? schema-manager :example/user s)))))))

