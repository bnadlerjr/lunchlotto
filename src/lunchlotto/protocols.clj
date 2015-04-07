(ns lunchlotto.protocols)

(defprotocol Venue
  (find-by-location [this latitude longitude]))

(defprotocol Email
  (send-confirmation-token [this email link]))
