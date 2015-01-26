(ns lunchlotto.settings.handlers
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.settings.views :as views]))

(defn show-settings
  [req]
  (respond-with/ok (views/show-settings)))
