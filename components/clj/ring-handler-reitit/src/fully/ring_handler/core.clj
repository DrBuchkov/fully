(ns fully.ring-handler.core
  (:require [reitit.ring :as ring]
            [fully.routes.api :refer [routes]]))

(def ring-handler (ring/ring-handler (ring/router routes)))
