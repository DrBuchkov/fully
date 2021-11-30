(ns fully.logger.core
  (:require [taoensso.timbre :as timbre]))

(defmacro log [level & args]
  `(timbre/log ~level ~@args))

(defmacro trace [& args]
  `(timbre/trace ~@args))

(defmacro debug [& args]
  `(timbre/debug ~@args))

(defmacro info [& args]
  `(timbre/info ~@args))

(defmacro warn [& args]
  `(timbre/warn ~@args))

(defmacro error [& args]
  `(timbre/error ~@args))

(defmacro fatal [& args]
  `(timbre/fatal ~@args))

(defmacro report [& args]
  `(timbre/report ~@args))

(defmacro logf [level & args]
  `(timbre/logf ~level ~@args))

(defmacro tracef [& args]
  `(timbre/tracef ~@args))

(defmacro debugf [& args]
  `(timbre/debugf ~@args))

(defmacro infof [& args]
  `(timbre/infof ~@args))

(defmacro warnf [& args]
  `(timbre/warnf ~@args))

(defmacro errorf [& args]
  `(timbre/errorf ~@args))

(defmacro fatalf [& args]
  `(timbre/fatalf ~@args))

(defmacro reportf [& args]
  `(timbre/reportf ~@args))