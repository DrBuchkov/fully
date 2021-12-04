(ns fully.repository-protocol.api)


(defprotocol IRepository
  (save! [this type resource-data])
  (exists? [this type id])
  (fetch! [this type id])
  (find! [this type] [this type opts])
  (update! [this type id resource-data])
  (delete! [this type id]))
