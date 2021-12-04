(ns fully.schema-manager-protocol.api)


(defprotocol ISchemaManager
  (entity-id-key [this type])
  (properties [this type])
  (valid? [this type data] [this type data opts])
  (generate [this type] [this type opts])
  (sample [this type] [this type opts]))
