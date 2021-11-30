(ns fully.test-helper.with-system
  (:require [com.stuartsierra.component :as component]))


(def ^:dynamic *system*)

(defn with-system [create-system]
  (fn [test-run]
    (binding [*system* (component/start-system (create-system))]
      (test-run)
      (component/stop-system *system*))))
