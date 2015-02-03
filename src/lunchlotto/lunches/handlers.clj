(ns lunchlotto.lunches.handlers
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.lunches.views :as views]
            [cemerick.friend :as friend]))

(defn show-upcoming
  [req]
  (respond-with/ok (views/upcoming {:current-user (friend/current-authentication req)})))

(defn show-pending
  [req]
  (respond-with/ok (views/pending {:current-user (friend/current-authentication req)})))

(defn show-recommended
  [req]
  (respond-with/ok (views/recommended {:current-user (friend/current-authentication req)})))
