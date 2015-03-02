(ns lunchlotto.common.utils
  (:require [clojure.edn :as edn]
            [environ.core :refer [env]]
            [postmark.core :as pm]))

(defn fmap
  "Applies a function to each value in the given map."
  [f coll]
  (into {} (for [[k v] coll] [k (f v)])))

(defn separate
  "Separate a collection into two collections using predicate function."
  [pred coll]
  (reduce (fn [[yes no] item]
            (if (pred item)
              [(conj yes item) no]
              [yes (conj no item)]))
          [[] []]
          coll))

(defn string->number
  "Convert a string to a number. Return the unaltered string if it does not
  look like a number."
  [s]
  (if (and (not (nil? s))
           (re-find #"^-?\d+\.?\d*$" s))
    (edn/read-string s)
    s))

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

(def make-email
  "Wraps Postmark email library."
  (pm/postmark (env :postmark-api-key) (env :postmark-from)))
