(ns lunchlotto.lunches.handlers
  (:require [lunchlotto.common.responses :as response]))

(defn show-upcoming
  [req]
  (response/render :ok [:lunches :upcoming]))

(defn show-pending
  [req]
  (response/render :ok [:lunches :pending]))

(defn show-recommended
  [req]
  (response/render :ok [:lunches :recommendations]))
