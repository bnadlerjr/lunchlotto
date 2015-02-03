(ns lunchlotto.settings.handlers
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.settings.views :as views]
            [cemerick.friend :as friend]))

(defn show-settings
  [req]
  (respond-with/ok (views/show-settings {:current-user (friend/current-authentication req)})))
