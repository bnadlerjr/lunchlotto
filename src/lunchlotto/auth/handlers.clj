(ns lunchlotto.auth.handlers
  (:require [compojure.core :refer [GET POST PUT routes]]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.common.responses :as respond-with]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.auth.views :as views]
            [lunchlotto.common.utils :as utils]))

(def db (env :database-url))

(defn show-registration-page
  "Show the new user registration page."
  [req]
  (respond-with/ok (views/register-page (:params req))))

(defn show-confirmation-page
  "Retrieve the user based on the confirmation token and render the
  confirmation page."
  [req]
  (let [token (get-in req [:params :confirmation_token])
        user (models/find-user-by-confirmation-token db token)]
    (if user
      (respond-with/ok (views/confirmation-page (:params req)))
      (respond-with/redirect "/" "Invalid confirmation token."))))

(defn register-user
  "Register a new user. Sends an email with a password token to the email
  address provided. Redirects to the home page with a message indicating that
  the user needs to check their email."
  [params]
  (let [[valid? data] (val/validate-email params)
        user (models/find-user-by-email db (:email data))]

    (cond (not valid?)
          (respond-with/bad-request (views/register-page data))

          (and user (:is_confirmed user))
          (respond-with/redirect "/" "That email has already been used. Please login.")

          (and user (not (:is_confirmed user)))
          (respond-with/ok (views/register-page
                           (assoc data
                             :errors {:email             "email has already been used."
                                      :can_resend_token? true})))
          :else
          (let [token (models/register-user db (:email data))]
            (if (env :debug false)
              (log/debug "Your token is:" token)
              ;; else send email
              )
            (respond-with/redirect "/" "You're almost done! You've just been an email that contains a confirmation link. Click the link in the email to complete your registration.")))))

(defn update-confirmation-token
  "Updates a user's confirmation token and re-sends confirmation email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))]
    (if valid?
      (let [token (models/update-confirmation-token db (:email data))]
        (if (env :debug false)
          (log/debug "Your updated token is:" token)
          ;; else send email
          )
        (respond-with/redirect "/" "You're almost done! You've just been an email that contains a confirmation link. Click the link in the email to complete your registration."))
      (respond-with/bad-request (views/register-page (assoc-in data [:errors :can_resend_token?] true))))))

(defn confirm-user
  "Finish the user registration and confirm the new user."
  [params]
  (let [[valid? data]
        (val/validate-registration
          (assoc params
            :latitude (utils/parse-number (:latitude params))
            :longitude (utils/parse-number (:longitude params))))]
    (if valid?
      (do
        (models/confirm-user db data)
        (respond-with/redirect "/" "Thanks for confirming your email. You are now fully registered."))
      (respond-with/bad-request (views/confirmation-page data)))))

(def auth-routes
  (routes
    (GET "/register" [] show-registration-page)
    (POST "/register" {params :params} (register-user params))
    (PUT "/register" [] update-confirmation-token)
    (GET "/confirm" [] show-confirmation-page)
    (POST "/confirm" {params :params} (confirm-user params))))
