(ns lunchlotto.lunches.views
  (:require [lunchlotto.common.views :refer [layout]]))

(defn upcoming
  [context]
  (layout context
    [:h1 "Upcoming Lunches"]))

(defn pending
  [context]
  (layout context
    [:h1 "Pending Lunches"]))

(defn recommended
  [context]
  (layout context
    [:h1 "Recommended Lunches"]))
