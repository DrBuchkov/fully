(ns fully.entity-utils.core
  (:require [fully.schema-manager-protocol.api :as scm]
            [fully.schema-utils.api :as su]))


(defn gen-id [schema-manager type entity]
  (let [{:keys [fully.entity/id-key]} (scm/properties schema-manager type)
        generated (scm/generate schema-manager type {:pipe #(su/select-keys % [id-key])})]
    (assoc (merge entity
                  generated)
      :xt/id (get generated id-key))))
