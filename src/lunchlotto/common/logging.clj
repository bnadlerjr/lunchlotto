(ns lunchlotto.common.logging
  (:require [clojure.tools.logging]))

(defn- to-keypair-string
  [[k v]]
  (let [format-str (if (re-find #"(\/|\s)" (str v))
                     "%s=\"%s\""
                     "%s=%s")]
    (format format-str (name k) v)))

(defn- log
  "Custom log messages in key/value pair format."
  ([level message]
    (log level message {}))
  ([level message attrs]
    (clojure.tools.logging/log
      level
      (clojure.string/join " " (map to-keypair-string
                                    (assoc attrs :at (name level)
                                                 :msg message))))))

(def trace (partial log :trace))
(def debug (partial log :debug))
(def info (partial log :info))
(def warn (partial log :warn))
(def error (partial log :error))
(def fatal (partial log :fatal))
