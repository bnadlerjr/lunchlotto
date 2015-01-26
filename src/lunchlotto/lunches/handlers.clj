(ns lunchlotto.lunches.handlers
  (:require [compojure.core :refer [GET routes]]
            [lunchlotto.common.responses :as respond-with]
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

(def lunch-routes
  (routes
    (GET "/lunches/upcoming" [] show-upcoming)
    (GET "/lunches/pending" [] show-pending)
    (GET "/lunches/recommended" [] show-recommended)))
