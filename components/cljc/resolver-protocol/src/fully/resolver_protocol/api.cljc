(ns fully.resolver-protocol.api)

(defprotocol IResolver
  (register [this op])
  (resolve! [this query] [this entity query]))
