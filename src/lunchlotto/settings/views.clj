(ns lunchlotto.settings.views
  (:require [hiccup.form :as hiccup]
            [ring.util.anti-forgery :as ring])
  (:require [lunchlotto.common.views :refer [layout]]
            [lunchlotto.common.forms :as forms]))

(defn show-settings
  [{:keys [:params] :as context}]
  (layout context
    [:h1 "Settings"]
    (forms/build-form :put "/settings" "Update Settings"
                      [:text-field "email"
                       {:label "Email"
                        :value (:email params)
                        :opts {:disabled true}}]

                      [:password-field "current_password"
                       {:label "Current Password"
                        :error (get-in params [:errors :current_password])
                        :description "Your current password is required to make any changes."
                        :opts  {:required true :autofocus true}}]

                      [:password-field "new_password"
                       {:label "New Password"
                        :error (get-in params [:errors :new_password])
                        :help-text "Optional - Leave this blank if you only need to update your location."}]

                      [:password-field "new_password_confirmation"
                       {:label "Retype New Password"
                        :error (get-in params [:errors :new_password_confirmation])
                        :help-text "Optional - Leave blank unless you are changing your password."}]

                      [:text-field "location"
                       {:label "Location"
                        :error (get-in params [:errors :location])
                        :value (:location params)}]

                      [:hidden-field "id" {:value (:id params)}]
                      [:hidden-field "latitude" {:value (:latitude params)}]
                      [:hidden-field "longitude" {:value (:longitude params)}])
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
