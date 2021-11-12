(ns fully.protocols.schema)

(defprotocol ISchemaManager
  (valid? [this type data] [this type data pipe])
  (generate [this type] [this type pipe]))