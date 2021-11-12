(ns fully.protocols.ring)

(defprotocol IRingHandlerProvider
  (getHandler [this]))
