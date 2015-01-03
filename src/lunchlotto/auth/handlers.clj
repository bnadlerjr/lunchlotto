(ns lunchlotto.auth.handlers
  (:require [prone.debug :refer [debug]])
  (:require [compojure.core :refer [GET POST PUT routes]]
            [ring.util.response :as ring]
            [environ.core :refer [env]])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.auth.views :as views]
            [lunchlotto.common.utils :as utils]))

(def db (env :database-url))

(defn bad-request
  "Returns a ring response with the given body, a status code of 400, and no
  headers."
  [body]
  {:status  400
   :headers {}
   :body    body})

(defn redirect-with-flash
  "Redirects to specified URL with the given flash message."
  [url flash]
  (assoc (ring/redirect url) :flash flash))

(defn show-registration-page
  "Show the new user registration page."
  [req]
  (ring/response (views/register-page (:params req))))

(defn show-confirmation-page
  "Retrieve the user based on the confirmation token and render the
  confirmation page."
  [req]
  (let [token (get-in req [:params :confirmation_token])
        user (models/find-user-by-confirmation-token db token)]
    (if user
      (ring/response (views/confirmation-page (:params req)))
      (redirect-with-flash "/" "Invalid confirmation token."))))

(defn register-user
  "Register a new user. Sends an email with a password token to the email
  address provided. Redirects to the home page with a message indicating that
  the user needs to check their email."
  [params]
  (let [[valid? data] (val/validate-email params)
        user (models/find-user-by-email db (:email data))]

    (cond (not valid?)
          (bad-request (views/register-page data))

          (and user (:is_confirmed user))
          (redirect-with-flash "/" "That email has already been used. Please login.")

          (and user (not (:is_confirmed user)))
          (ring/response (views/register-page
                           (assoc data
                             :errors {:email             "email has already been used."
                                      :can_resend_token? true})))
          :else
          (let [token (models/register-user db (:email data))]
            (if (env :debug false)
              (println (str "Your token is: " token))
              ;; else send email
              )
            (redirect-with-flash "/" "You're almost done! You've just been an email that contains a confirmation link. Click the link in the email to complete your registration.")))))

(defn update-confirmation-token
  "Updates a user's confirmation token and re-sends confirmation email."
  [req]
  (let [[valid? data] (val/validate-email (:params req))]
    (if valid?
      (let [token (models/update-confirmation-token db (:email data))]
        (if (env :debug false)
          (println (str "Your updated token is: " token))
          ;; else send email
          )
        (redirect-with-flash "/" "You're almost done! You've just been an email that contains a confirmation link. Click the link in the email to complete your registration."))
      (bad-request (views/register-page (assoc-in data [:errors :can_resend_token?] true))))))

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
        (redirect-with-flash "/" "Thanks for confirming your email. You are now fully registered."))
      (bad-request (views/confirmation-page data)))))

(def auth-routes
  (routes
    (GET "/register" [] show-registration-page)
    (POST "/register" {params :params} (register-user params))
    (PUT "/register" [] update-confirmation-token)
    (GET "/confirm" [] show-confirmation-page)
    (POST "/confirm" {params :params} (confirm-user params))))
