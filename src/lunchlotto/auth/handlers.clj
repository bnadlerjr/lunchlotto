(ns lunchlotto.auth.handlers
  (:require [environ.core :refer [env]]
            [cemerick.friend :as friend])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.common.responses :as respond-with]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.auth.views :as views]
            [lunchlotto.common.utils :as utils]
            [lunchlotto.auth.email :as email]))

(def db (env :database-url))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :auth))

(defn show-registration-page
  "Show the new user registration page."
  [req]
  (respond-with/ok (views/register-page (select-keys req [:params]))))

(defn show-login-page
  "Show the user login page."
  [_]
  (respond-with/ok (views/login-page)))

(defn show-confirmation-page
  "Retrieve the user based on the confirmation token and render the
  confirmation page."
  [req]
  (let [token (get-in req [:params :confirmation_token])
        user (models/find-user-by-confirmation-token db token)]
    (if user
      (respond-with/ok
        (views/confirmation-page
          (assoc-in (select-keys req [:params]) [:params :email] (:email user))))
      (respond-with/redirect "/" (t [:flash :invalid-token])))))

(defn register-user
  "Register a new user. Sends an email with a password token to the email
  address provided. Redirects to the home page with a message indicating that
  the user needs to check their email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))
        user (models/find-user-by-email db (:email data))]

    (cond (not valid?)
          (respond-with/bad-request (views/register-page {:params data}))

          (and user (:is_confirmed user))
          (respond-with/redirect "/" (t [:flash :email-used]))

          (and user (not (:is_confirmed user)))
          (respond-with/ok
            (views/register-page
              {:params (assoc data :errors
                                   {:email             (t [:validations :email :used])
                                    :can_resend_token? true})}))
          :else
          (let [token (models/register-user db (:email data))]
            (email/send-confirmation-email (:email data) token req)
            (respond-with/redirect "/" (t [:flash :confirmation-sent]))))))

(defn update-confirmation-token
  "Updates a user's confirmation token and re-sends confirmation email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))]
    (if valid?
      (let [token (models/update-confirmation-token db (:email data))]
        (email/send-confirmation-email (:email data) token req)
        (respond-with/redirect "/" (t [:flash :confirmation-sent])))
      (respond-with/bad-request
        (views/register-page
          {:params (assoc-in data [:errors :can_resend_token?] true)})))))

(defn confirm-user
  "Finish the user registration and confirm the new user."
  [req]
  (let [params (:params req)
        [valid? data]
        (val/validate-registration
          (assoc params
            :latitude (utils/parse-number (:latitude params))
            :longitude (utils/parse-number (:longitude params))))]
    (if valid?
      (do
        (models/confirm-user db data)
        (friend/merge-authentication
          (respond-with/redirect "/lunches/upcoming" (t [:flash :registered]))
          {:username (:email data)
           :roles #{::user}}))
      (respond-with/bad-request (views/confirmation-page {:params data})))))

(defn authenticate
  "Authenticates a user with the given username (email) and password."
  [{:keys [username password]}]
  (when-let [user (models/authenticate-user db username password)]
    (assoc (select-keys user [:id :email]) :username username
                                           :roles #{::user})))

(defn failed-login
  [_]
  (respond-with/bad-request (views/login-page {:flash (t [:flash :invalid-creds])})))
