(ns lunchlotto.common.utils
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]))

(defn parse-number
  "Convert a string to a number. Return nil if not a number.

  Since read-string is used, it checks the string against a regex to ensure the
  string looks like a number."
  [s]
  (if (and (not (nil? s))
           (re-find #"^-?\d+\.?\d*$" s))
    (read-string s)))

(defn- to-keypair-string
  [[k v]]
  (let [format-str (if (re-find #"(\/|\s)" (str v))
                     "%s=\"%s\""
                     "%s=%s")]
    (format format-str (name k) v)))

(defn- log
  "Creates a log message. Any attrs are converted to string key/value pairs in
  the following format \"k=v\"."
  ([level message]
    (log/log level (s/join " " (map to-keypair-string {:at (name level)
                                                       :msg message}))))
  ([level message attrs]
    (log/log level (s/join " " (map to-keypair-string
                                    (assoc attrs :at (name level)
                                                 :msg message))))))

(def trace (partial log :trace))
(def debug (partial log :debug))
(def info (partial log :info))
(def warn (partial log :warn))
(def error (partial log :error))
(def fatal (partial log :fatal))
