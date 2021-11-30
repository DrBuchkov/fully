(ns fully.middleware.core
  (:require [muuntaja.middleware :as mm]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :as rmd]
            [fully.errors.api :refer [wrap-errors]]))

(defn wrap-middleware [handler]
  (-> handler
      (rmd/wrap-defaults rmd/api-defaults)
      wrap-errors
      #_(wrap-cors :access-control-allow-origin [#".*"]
                   :access-control-allow-methods [:get :put :post :delete :patch])
      mm/wrap-format))