(ns lunchlotto.settings.views
  (:require [hiccup.form :as hiccup]
            [ring.util.anti-forgery :as ring])
  (:require [lunchlotto.common.views :refer [layout]]
            [lunchlotto.common.forms :as forms]))

(defn show-settings
  [{:keys [:params] :as context}]
  (layout context
    [:h1 "Settings"]
    [:hr]
    [:div.panel.panel-danger
    [:div.panel-heading
     [:h3.panel-title "Delete Account"]]
     [:div.panel-body
      [:p "Warning! This cannot be undone!"]
      (hiccup/form-to
        [:delete "/settings"]
        (ring/anti-forgery-field)
        [:div.form-group
         (hiccup/submit-button {:class "btn btn-danger"} "Delete Account")])]]))
