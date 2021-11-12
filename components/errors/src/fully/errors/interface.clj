(ns fully.errors.interface
  (:require [fully.errors.core :as core]))

(defn wrap-errors [handler]
  (core/wrap-errors handler))

(defn err! [type message err-data]
  (core/err! type message err-data))

(defn not-found! [{:keys [id] :as err-data}]
  (core/not-found! err-data))

(defn validation-error! [{:keys [resource type] :as err-data}]
  (core/validation-error! err-data))