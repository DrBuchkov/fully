(ns fully.config.core
  (:require [aero.core :as aero]
            #?(:clj [clojure.java.io :as io])))

(defn source [path]
  #?(:clj  (io/resource path)
     :cljs path))

(def env (-> "env.edn"
             source
             aero/read-config))