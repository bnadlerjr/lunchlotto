(ns lunchlotto.lunches.handlers
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.lunches.views :as views]))

(defn show-upcoming
  [req]
  (respond-with/ok (views/upcoming)))

(defn show-pending
  [req]
  (respond-with/ok (views/pending)))

(defn show-recommended
  [req]
  (respond-with/ok (views/recommended)))
