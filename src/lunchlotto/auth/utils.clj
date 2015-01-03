(ns lunchlotto.auth.utils
  (:require [crypto.password.bcrypt :as password]
            [crypto.random :as crypt]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]
            [pandect.algo.sha512 :as pandect]))

(defn digest
  "Wrapper for pandect.algo.sha512 encryption function."
  [s]
  (pandect/sha512 s))

(defn generate-token
  "Generates a URL friendly token and expiration date as a timestamp. The
  default expiration is 30 minutes."
  []
  [(crypt/url-part 24)
   (coerce/to-timestamp (time/plus (time/now) (time/minutes 30)))])

(defn encrypt-password
  "Encrypts given password using brcrypt. Wrapper for
  crypto.password.bcrypt/encrypt."
  [password]
  (password/encrypt password))

(defn check-password
  "Check that raw password matches encrypted password. Returns true if they
  match, otherwise false. Wrapper for crypto.password.bcrypt/check function."
  [raw-password encrypted-password]
  (password/check raw-password encrypted-password))
