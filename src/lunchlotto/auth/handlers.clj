(ns lunchlotto.auth.handlers
  (:require [environ.core :refer [env]]
            [cemerick.friend :as friend])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.common.responses :as response]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.common.utils :as utils]
            [lunchlotto.auth.email :as email]))

(def db (env :database-url))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :auth))

(defn show-registration-page
  "Show the new user registration page."
  [req]
  (response/render :ok "auth/register" {:user (:params req)}))

(defn show-login-page
  "Show the user login page."
  [_]
  (response/render :ok "auth/login"))

(defn show-confirmation-page
  "Retrieve the user based on the confirmation token and render the
  confirmation page."
  [req]
  (let [token (get-in req [:params :confirmation_token])
        user (models/find-user-by-confirmation-token {:token token})]
    (if user
      (response/render :ok "auth/confirm"
                       {:user (assoc (:params req) :email (:email user))})
      (response/redirect "/" (t [:flash :invalid-token])))))

(defn register-user
  "Register a new user. Sends an email with a password token to the email
  address provided. Redirects to the home page with a message indicating that
  the user needs to check their email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))
        user (models/find-user-by-email {:email (:email data)})]

    (cond (not valid?)
          (response/render :bad-request "auth/register" {:user data})

          (and user (:is_confirmed user))
          (response/redirect "/" (t [:flash :email-used]))

          (and user (not (:is_confirmed user)))
          (response/render :ok "auth/register"
                           {:user (assoc data :errors
                                         {:email             [(t [:validations :email :used])]
                                          :can_resend_token? true})})
          :else
          (let [token (models/register-user (:email data))]
            (email/send-confirmation-email (:email data) token req)
            (response/redirect "/" (t [:flash :confirmation-sent]))))))

(defn update-confirmation-token
  "Updates a user's confirmation token and re-sends confirmation email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))]
    (if valid?
      (let [token (models/update-confirmation-token (:email data))]
        (email/send-confirmation-email (:email data) token req)
        (response/redirect "/" (t [:flash :confirmation-sent])))
      (response/render :bad-request "auth/register"
                       {:user (assoc-in data [:errors :can_resend_token?] true)}))))

(defn confirm-user
  "Finish the user registration and confirm the new user."
  [req]
  (let [[valid? data] (val/validate-registration (:params req))]
    (if valid?
      (do
        (models/confirm-user db data)
        (let [user (models/find-user-by-email {:email (:email data)})]
          (friend/merge-authentication
            (response/redirect "/lunches/upcoming" (t [:flash :registered]))
            {:id (:id user)
             :username (:email data)
             :roles #{::user}})))
      (response/render :bad-request "auth/confirm" {:user data}))))

(defn authenticate
  "Authenticates a user with the given username (email) and password."
  [{:keys [username password]}]
  (when-let [user (models/authenticate-user db username password)]
    (assoc (select-keys user [:id]) :username username :roles #{::user})))

(defn failed-login
  [_]
  (response/redirect "/login" (t [:flash :invalid-creds])))
