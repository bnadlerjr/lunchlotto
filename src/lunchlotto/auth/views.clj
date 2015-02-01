(ns lunchlotto.auth.views
  (:require [hiccup.element :as e])
  (:require [lunchlotto.common.forms :as forms]
            [lunchlotto.common.views :refer [layout]]))

(defn register-page
  "User registration page."
  [params]
  (let [options (if (get-in params [:errors :can_resend_token?])
                  {:button-text "Re-send Confirmation Email" :method :put}
                  {:button-text "Register" :method :post})]
    (layout
      [:h2 "Register New User"]
      (forms/build-form (:method options) "/register" (:button-text options)
                        [:text-field "email"
                         {:label "Cyrus Email"
                          :error (get-in params [:errors :email])
                          :value (:email params)
                          :opts  {:required true :autofocus true}}]))))

(defn confirmation-page
  "Allows user to complete their registration by providing a password and
  location."
  [params]
  (layout
    [:h2 "Finish Registration"]
    [:p "Thanks for confirming your email address! Follow the steps below to finish your registration."]
    (forms/build-form :post "/confirm" "Finish Registration"
                      [:password-field "password"
                       {:label "Password"
                        :error (get-in params [:errors :password])
                        :value (:password params)
                        :opts  {:required true :autofocus true}}]

                      [:password-field "password_confirmation"
                       {:label "Retype Password"
                        :error (get-in params [:errors :password_confirmation])
                        :opts  {:required true}}]

                      [:text-field "location"
                       {:label       "Location"
                        :error       (get-in params [:errors :location])
                        :value       (:location params)
                        :description "In order to recommend lunches with people near you, we need to know your location. You can always change it later."}]

                      [:hidden-field "email" {:value (:email params)}]
                      [:hidden-field "confirmation_token" {:value (:confirmation_token params)}]
                      [:hidden-field "latitude" {:value (:latitude params)}]
                      [:hidden-field "longitude" {:value (:longitude params)}])))

(defn login-page
  "Allows user to login."
  [& flash]
  (layout
    (when-not (nil? flash) [:div {:class "alert alert-info"} flash])
    [:h2 "Login"]
    [:p "New here? " (e/link-to "/register" "Register") " for an account."]
    (forms/build-form :post "/login" "Login"
                      [:text-field "username"
                       {:label "Email"
                        :opts  {:required true :autofocus true}}]

                      [:password-field "password" {:label "Password"}])))
