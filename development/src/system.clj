(ns system
  (:require [clojure.tools.namespace.repl :as tn]))

(tn/disable-reload!)

(defonce system (atom nil))
