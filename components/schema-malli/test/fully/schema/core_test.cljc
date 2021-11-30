(ns fully.schema.core-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [fully.test-helper.with-system :refer :all]
            [fully.schema.core :as sut]
            [fully.protocols.api :as proto]
            [com.stuartsierra.component :as component]))

(defn create-system []
  (component/system-map
    :schema-manager (sut/create-schema-manager)))

(use-fixtures :each (with-system create-system))

(deftest schema-manager-test
  (testing "Generated value is valid"
    (let [{:keys [schema-manager]} *system*
          value (proto/generate schema-manager :example/user)]
      (is (= true (proto/valid? schema-manager :example/user value)))))
  (testing "Generated sample is valid"
    (let [{:keys [schema-manager]} *system*
          sample (proto/sample schema-manager :example/user)]
      (doseq [s sample]
        (is (= true (proto/valid? schema-manager :example/user s)))))))

