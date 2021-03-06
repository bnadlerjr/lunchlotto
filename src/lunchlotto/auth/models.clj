(ns lunchlotto.auth.models
  (:require [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [clojure.java.jdbc :as jdbc])
  (:require [lunchlotto.auth.utils :as utils]
            [lunchlotto.common.queries :as q]))

(defn register-user
  "Register a new user in the database. Assumes that the email address has
  already been validated. Returns a registration token."
  [email]
  (let [[token expires] (utils/generate-token)]
    (q/insert-user! {:email email
                     :confirmation_token (utils/digest token)
                     :confirmation_token_expires_at expires})
    token))

(defn find-user-by-confirmation-token
  "Finds a user by their registration token. If the token cannot be found or is
   expired, returns nil. Digests the token before making the query."
  [token]
  (first (q/find-user-by-confirmation-token {:token (utils/digest token)})))

(defn find-user-by-email
  "Finds user by their email address. Returns nil if no user is found."
  [email]
  (first (q/find-user-by-email {:email email})))

(defn confirm-user
  "Confirms a user and completes their registration. Returns true if
  successful, otherwise false. Assumes params have already been validated.

  Finds a user by confirmation token and does the following:
      * stores location, latitude and longitude
      * encrypts and stores password
      * sets 'is_confirmed' flag and confirmation date
      * sets confirmation token and expiry to null"
  [db params]
  (= [1]
     (jdbc/update!
       db
       :users {:location (:location params)
               :latitude (:latitude params)
               :longitude (:longitude params)
               :password (utils/encrypt-password (:password params))
               :is_confirmed true
               :confirmed_at (coerce/to-timestamp (time/now))
               :confirmation_token nil
               :confirmation_token_expires_at nil}
       ["confirmation_token=? AND confirmation_token_expires_at>? AND email=?"
        (utils/digest (:confirmation_token params))
        (coerce/to-timestamp (time/now))
        (:email params)])))

(defn update-confirmation-token
  "Updates a user's confirmation token and sets a new expiry. Assumes that the
  email address has already been validated. Returns the new token. Returns nil
  if the email could not be found or if the user has already been confirmed."
  [email]
  (let [[token expires] (utils/generate-token)]
    (when (= [1]
             (q/update-user-confirmation-token!
               {:confirmation_token (utils/digest token)
                :confirmation_token_expires_at expires
                :email email}))
      token)))

(defn authenticate-user
  "Authenticates user by checking password. Only users that are confirmed can
  be authenticated."
  [db email password]
  (let [user (first (jdbc/query db ["SELECT *
                                     FROM users
                                     WHERE is_confirmed=true
                                       AND email=?" email]))]
    (when (and user
               (utils/check-password password (:password user)))
      user)))
