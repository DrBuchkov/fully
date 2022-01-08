(ns fully.schema-manager-protocol.api)


(defprotocol ISchemaManager
  (properties [this type])
  (valid? [this type data] [this type data opts])
  (generate [this type] [this type opts])
  (children [this type])
  (sample [this type] [this type opts]))
