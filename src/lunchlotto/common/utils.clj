(ns lunchlotto.common.utils)

(defn parse-number
  "Convert a string to a number. Return nil if not a number.

  Since read-string is used, it checks the string against a regex to ensure the
  string looks like a number."
  [s]
  (if (and (not (nil? s))
           (re-find #"^-?\d+\.?\d*$" s))
    (read-string s)))
