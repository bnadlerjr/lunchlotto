(ns lunchlotto.protocols)

(defprotocol Venue
  (find-by-location [this latitude longitude]))
