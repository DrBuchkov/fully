(ns fully.repository-protocol.api
  (:refer-clojure :exclude [update find]))


(defprotocol IRepository
  (save [this type entity])
  (exists? [this type id])
  (fetch [this type id] [this type id opts])
  (find [this type] [this type opts])
  (update [this type id entity-data])
  (delete [this type id])
  (flush! [this])
  (flush-async! [this]))
