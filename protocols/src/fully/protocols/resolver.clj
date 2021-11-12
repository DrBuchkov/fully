(ns fully.protocols.resolver)


(defprotocol IResolver
  (register-resolver [this op])
  (resolve! [this query] [this entity query]))
