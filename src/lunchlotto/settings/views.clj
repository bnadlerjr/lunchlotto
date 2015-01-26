(ns lunchlotto.settings.views
  (:require [lunchlotto.common.views :refer [authenticated-layout]]))

(defn show-settings
  []
  (authenticated-layout
    [:h1 "Settings"]))
