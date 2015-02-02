(ns lunchlotto.lunches.views
  (:require [lunchlotto.common.views :refer [authenticated-layout]]))

(defn upcoming
  []
  (authenticated-layout {}
    [:h1 "Upcoming Lunches"]))

(defn pending
  []
  (authenticated-layout {}
    [:h1 "Pending Lunches"]))

(defn recommended
  []
  (authenticated-layout {}
    [:h1 "Recommended Lunches"]))
