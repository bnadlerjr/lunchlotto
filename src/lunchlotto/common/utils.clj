(ns lunchlotto.common.utils
  (:require [clojure.edn :as edn]))

(defn parse-number
  "Convert a string to a number. Return nil if not a number.

  Since read-string is used, it checks the string against a regex to ensure the
  string looks like a number."
  [s]
  (if (and (not (nil? s))
           (re-find #"^-?\d+\.?\d*$" s))
    (read-string s)))

(defn make-translation-dictionary
  "Loads the given EDN file and returns a function that can be used to look up
  translation keys from the loaded file. If path is given, it is prepended to
  the list of arguments given in the returned translation function.

  TODO: What are the pros/cons of memoizing this? Is there a better way?"
  [filename & path]
  (let [dictionary (edn/read-string (slurp filename))]
    (fn
      [args]
      (get-in dictionary (concat path args)))))
