(ns fully.schema-utils.api
  (:refer-clojure :exclude [merge select-keys get get-in dissoc assoc update assoc-in update-in])
  (:require [fully.schema-utils.core :as core]))

(def equals core/equals)

(def find-first core/find-first)

(def merge core/merge)

(def union core/union)

(def update-properties core/update-properties)

(def closed-schema core/closed-schema)

(def open-schema core/open-schema)

(def subschemas core/subschemas)

(def distinct-by core/distinct-by)

(def path->in core/path->in)

(def in->paths core/in->paths)

(def transform-entries core/transform-entries)

(def optional-keys core/optional-keys)

(def required-keys core/required-keys)

(def select-keys core/select-keys)

(def dissoc core/dissoc)

(def get core/get)

(def assoc core/assoc)

(def update core/update)

(def get-in core/get-in)

(def assoc-in core/assoc-in)

(def update-in core/update-in)

(def to-map-syntax core/to-map-syntax)

(def from-map-syntax core/from-map-syntax)

(def schemas core/schemas)
