(ns fully.logger.interface
  (:require [fully.logger.core :as core]))

(defmacro log [level & args]
  `(core/log ~level ~@args))

(defmacro trace [& args]
  `(core/trace ~@args))

(defmacro debug [& args]
  `(core/debug ~@args))

(defmacro info [& args]
  `(core/info ~@args))

(defmacro warn [& args]
  `(core/warn ~@args))

(defmacro error [& args]
  `(core/error ~@args))

(defmacro fatal [& args]
  `(core/fatal ~@args))

(defmacro report [& args]
  `(core/report ~@args))

(defmacro logf [level & args]
  `(core/logf ~level ~@args))

(defmacro tracef [& args]
  `(core/tracef ~@args))

(defmacro debugf [& args]
  `(core/debugf ~@args))

(defmacro infof [& args]
  `(core/infof ~@args))

(defmacro warnf [& args]
  `(core/warnf ~@args))

(defmacro errorf [& args]
  `(core/errorf ~@args))

(defmacro fatalf [& args]
  `(core/fatalf ~@args))

(defmacro reportf [& args]
  `(core/reportf ~@args))
