(ns fully.entity-manager-protocol.api)


(defprotocol IEntityManager
  (prepare [this type entity])
  (gen-id [this type entity]))
