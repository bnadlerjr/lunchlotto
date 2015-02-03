(ns lunchlotto.settings.views
  (:require [lunchlotto.common.views :refer [layout]]))

(defn show-settings
  [context]
  (layout context
    [:h1 "Settings"]))
