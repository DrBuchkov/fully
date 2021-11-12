(ns fully.errors.core
  (:require [ring.util.http-response :refer [ok
                                             not-found
                                             bad-request
                                             unauthorized
                                             forbidden
                                             internal-server-error]]
            [fully.logger.interface :as log])
  (:import (clojure.lang ExceptionInfo)))

(def err-data (comp :fully.http.error/data ex-data))

(defn wrap-errors [handler]
  (fn [req]
    (try
      (handler req)

      (catch ExceptionInfo e
        (let [exception-data (ex-data e)]
          (case (:fully.http.error/type exception-data)
            :fully.http.error/not-found (not-found exception-data)
            :fully.http.error/validation-error (bad-request exception-data)
            :fully.http.error/unauthorized (unauthorized exception-data)
            :fully.http.error/forbidden (forbidden exception-data)
            (do
              (log/error e)
              (internal-server-error "Oops, something went wrong")))))

      (catch Exception e
        (log/error e)
        (internal-server-error "Oops, something went wrong")))))


(defn err! [type message err-data]
  (throw (ex-info message {:fully.http.error/type type
                           :fully.http.error/data err-data})))

(defn not-found! [{:keys [id] :as err-data}]
  (err! :fully.http.error/not-found
        (str "No resource found with id " id)
        err-data))

(defn validation-error! [{:keys [resource type] :as err-data}]
  (err! :fully.http.error/validation-error
        "Data do not conform with schema"
        err-data))


(defn forbidden! []
  (err! :fully.http.error/forbidden
        "No access to this content"
        {}))

(defn not-implemented! []
  (err! :fully.http.error/not-implemented
        "The request method is not supported by the server and cannot be handled. "
        {}))