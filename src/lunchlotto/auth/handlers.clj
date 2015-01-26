(ns lunchlotto.auth.handlers
  (:require [environ.core :refer [env]])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.common.responses :as respond-with]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.auth.views :as views]
            [lunchlotto.common.logging :as logging]
            [lunchlotto.common.utils :as utils]
            [lunchlotto.auth.utils :as auth-utils]))

(def db (env :database-url))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :auth))

(defn show-registration-page
  "Show the new user registration page."
  [req]
  (respond-with/ok (views/register-page (:params req))))

(defn show-login-page
  "Show the user login page."
  [req]
  (respond-with/ok (views/login-page)))

(defn show-confirmation-page
  "Retrieve the user based on the confirmation token and render the
  confirmation page."
  [req]
  (let [token (get-in req [:params :confirmation_token])
        user (models/find-user-by-confirmation-token db token)]
    (if user
      (respond-with/ok (views/confirmation-page (:params req)))
      (respond-with/redirect "/" (t [:flash :invalid-token])))))

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
          (respond-with/redirect "/" (t [:flash :email-used]))

          (and user (not (:is_confirmed user)))
          (respond-with/ok
            (views/register-page
              (assoc data :errors {:email             (t [:validations :email :used])
                                   :can_resend_token? true})))
          :else
          (let [token (models/register-user db (:email data))]
            (if (env :debug false)
              (logging/debug (str "Your token is:" token))
              ;; else send email
              )
            (respond-with/redirect "/" (t [:flash :confirmation-sent]))))))

(defn update-confirmation-token
  "Updates a user's confirmation token and re-sends confirmation email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))]
    (if valid?
      (let [token (models/update-confirmation-token db (:email data))]
        (if (env :debug false)
          (logging/debug (str "Your updated token is:" token))
          ;; else send email
          )
        (respond-with/redirect "/" (t [:flash :confirmation-sent])))
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
        (respond-with/redirect "/" (t [:flash :registered])))
      (respond-with/bad-request (views/confirmation-page data)))))

(defn authenticate
  "Authenticates a user with the given username (email) and password."
  [{:keys [username password]}]
  (let [user (models/find-user-by-email db username)]
    (when (and user
               (auth-utils/check-password password (:password user)))
      (assoc user :username username
                  :roles #{::user}))))

(defn failed-login
  [req]
  (respond-with/bad-request (views/login-page (t [:flash :invalid-creds]))))
