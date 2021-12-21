(ns fully.schema-utils.core
  (:refer-clojure :exclude [merge select-keys get get-in dissoc assoc update assoc-in update-in])
  (:require [malli.util :as mu]))


(def equals mu/equals)

(def find-first mu/find-first)

(def merge mu/merge)

(def union mu/union)

(def update-properties mu/update-properties)

(def closed-schema mu/closed-schema)

(def open-schema mu/open-schema)

(def subschemas mu/subschemas)

(def distinct-by mu/distinct-by)

(def path->in mu/path->in)

(def in->paths mu/in->paths)

(def transform-entries mu/transform-entries)

(def optional-keys mu/optional-keys)

(def required-keys mu/required-keys)

(def select-keys mu/select-keys)

(def dissoc mu/dissoc)

(def get mu/get)

(def assoc mu/assoc)

(def update mu/update)

(def get-in mu/get-in)

(def assoc-in mu/assoc-in)

(def update-in mu/update-in)

(def to-map-syntax mu/to-map-syntax)

(def from-map-syntax mu/from-map-syntax)

(def schemas mu/schemas)

