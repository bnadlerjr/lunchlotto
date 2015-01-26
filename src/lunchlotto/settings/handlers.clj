(ns lunchlotto.settings.handlers
  (:require [compojure.core :refer [GET routes]]
            [lunchlotto.common.responses :as respond-with]
            [lunchlotto.settings.views :as views]))

(defn show-settings
  [req]
  (respond-with/ok (views/show-settings)))

(def settings-routes
  (routes
    (GET "/settings" [] show-settings)))
