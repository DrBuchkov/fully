(ns fully.protocols.schema)

(defprotocol ISchemaManager
  (properties [this type])
  (valid? [this type data] [this type data opts])
  (generate [this type] [this type opts])
  (sample [this type] [this type opts]))