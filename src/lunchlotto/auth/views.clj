(ns lunchlotto.auth.views
  (:require [hiccup.form :as f]
            [ring.util.anti-forgery :as ring])
  (:require [lunchlotto.common.views :refer [layout]]))

(defn- make-field [func name {:keys [:label :value :error :description :opts]}]
  [:div.form-group
   {:class (when error "has-error")}
   (f/label {:class "control-label"} name label)
   (when description [:p description])
   (func (merge {:id name :class "form-control" :value value} opts) name)
   (when error [:span.help-block error])])

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
        (make-field f/text-field "email" {:label "Cyrus Email"
                                          :error (get-in params [:errors :email])
                                          :value (:email params)
                                          :opts {:required true :autofocus true}})

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

               (make-field f/password-field "password" {:label "Password"
                                                        :error (get-in params [:errors :password])
                                                        :value (:password params)
                                                        :opts {:required true :autofocus true}})

               (make-field f/password-field "password_confirmation" {:label "Retype Password"
                                                                     :error (get-in params [:errors :password_confirmation])
                                                                     :opts {:required true}})

               (make-field f/text-field "location" {:label "Location"
                                                    :error (get-in params [:errors :location])
                                                    :value (:location params)
                                                    :description "In order to recommend lunches with people near you, we need to know your location. You can always change it later."})

               (f/hidden-field {:id "confirmation_token" :value (:confirmation_token params)} "confirmation_token")
               (f/hidden-field {:id "latitude" :value (:latitude params)} "latitude")
               (f/hidden-field {:id "longitude" :value (:longitude params)} "longitude")

               [:div.form-group
                (f/submit-button {:class "btn btn-primary"} "Finish Registration")])))
