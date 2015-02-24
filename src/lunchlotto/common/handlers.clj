(ns lunchlotto.common.handlers
  (:require [lunchlotto.common.responses :as response]))

(defn home
  "The home page handler."
  [req]
  (response/render :ok [:common :home]))
