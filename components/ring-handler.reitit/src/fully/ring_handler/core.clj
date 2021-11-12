(ns fully.ring-handler.core
  (:require [reitit.ring :as ring]
            [com.stuartsierra.component :as component]
            [fully.protocols.ring :refer [IRingHandlerProvider]]))

(defrecord ReititRingHandler [routes handlerfn]

  component/Lifecycle
  (start [this]
    (assoc this :handlerfn (ring/ring-handler
                             (ring/router
                               routes))))

  (stop [this]
    (assoc this :handlerfn nil))

  IRingHandlerProvider
  (getHandler [this] handlerfn))

(defn create-ring-handler [routes]
  (map->ReititRingHandler {:routes routes}))
