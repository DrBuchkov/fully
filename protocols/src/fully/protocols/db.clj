(ns fully.protocols.db)

(defprotocol IDatabaseManager
  (create-resource! [this type resource-data])
  (get-resource! [this type id])
  (list-resources! [this type])
  (put-resource! [this type id resource-data])
  (patch-resource! [this type id resource-data])
  (delete-resource! [this type id]))
