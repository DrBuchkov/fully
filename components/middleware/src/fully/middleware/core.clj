(ns fully.middleware.core
  (:require [muuntaja.middleware :as middleware]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.cors :refer [wrap-cors]]
            [fully.errors.interface :refer [wrap-errors]]))

(defn wrap-middleware [handler]
  (-> handler
      wrap-params
      wrap-errors
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete :patch])
      middleware/wrap-format))