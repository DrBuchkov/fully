(ns fully.schema.core-test
  (:require #?(:clj  [clojure.test :refer [deftest is testing use-fixtures]]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [fully.test.system :refer :all]
            [fully.schema.core :as sut]
            [fully.protocols.schema :as s]
            [com.stuartsierra.component :as component]))

(defn create-system []
  (component/system-map
    :schema-manager (sut/create-schema-manager)))

(use-fixtures :each (with-system create-system))

(deftest validate-test
  (let [{:keys [schema-manager]} *system*
        sample (s/generate schema-manager :example/user)]
    (testing "Generated sample is valid"
      (is (s/valid? schema-manager :example/user sample)))))

