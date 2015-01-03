(ns lunchlotto.auth.views
  (:require [prone.debug :refer [debug]])
  (:require [hiccup.form :as f]
            [ring.util.anti-forgery :as ring])
  (:require [lunchlotto.common.views :refer [layout]]))

(defn register-page
  "User registration page."
  [params]
  (let [options (if (get-in params [:errors :can_resend_token?])
                  {:button-text "Re-send Confirmation Email" :method :put}
                  {:button-text "Register" :method :post})]
    (layout
      [:h2 "Register New User"]
      (f/form-to
        [(:method options) "/register"]
        (ring/anti-forgery-field)
        [:div.form-group
         {:class (when (get-in params [:errors :email]) "has-error")}
         (f/label {:class "control-label"} "email" "Cyrus Email")
         (f/text-field {:id "email" :class "form-control" :value (:email params) :required true :autofocus true} "email")
         (when-let [errors (get-in params [:errors :email])] [:span.help-block errors])]

        [:div.form-group
         (f/submit-button {:class "btn btn-primary"} (:button-text options))]))))

(defn confirmation-page
  "Allows user to complete their registration by providing a password and
  location."
  [params]
  (layout
    [:h2 "Finish Registration"]
    [:p "Thanks for confirming your email address! Follow the steps below to finish your registration."]
    (f/form-to [:post "/confirm"]
               (ring/anti-forgery-field)

               [:div.form-group
                {:class (when (get-in params [:errors :password]) "has-error")}
                (f/label {:class "control-label"} "password" "Password")
                (f/password-field {:id "password" :class "form-control" :required true :autofocus true} "password")
                (when-let [errors (get-in params [:errors :password])] [:span.help-block errors])]

               [:div.form-group
                {:class (when (get-in params [:errors :password_confirmation]) "has-error")}
                (f/label {:class "control-label"} "password_confirmation" "Retype Password")
                (f/password-field {:id "password_confirmation" :class "form-control" :required true} "password_confirmation")
                (when-let [errors (get-in params [:errors :password_confirmation])] [:span.help-block errors])]

               [:div.form-group
                {:class (when (get-in params [:errors :location]) "has-error")}
                (f/label {:class "control-label"} "location" "Enter your location")
                [:p "In order to recommend lunches with people near you, we need to know your location. You can always change it later."]
                (f/text-field {:id "location" :class "form-control" :value (:location params)} "location")
                (when-let [errors (get-in params [:errors :location])] [:span.help-block errors])]

               (f/hidden-field {:id "confirmation_token" :value (:confirmation_token params)} "confirmation_token")
               (f/hidden-field {:id "latitude" :value (:latitude params)} "latitude")
               (f/hidden-field {:id "longitude" :value (:longitude params)} "longitude")

               [:div.form-group
                (f/submit-button {:class "btn btn-primary"} "Finish Registration")])))
