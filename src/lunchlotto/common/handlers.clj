(ns lunchlotto.common.handlers
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.common.views :as views]))

(defn home
  "The home page handler."
  [req]
  (respond-with/ok (views/home-page (select-keys req [:flash]))))
