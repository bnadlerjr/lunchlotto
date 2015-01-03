(ns lunchlotto.auth.validations
  (:require [bouncer.core :as b]
            [bouncer.validators :as v]
            [environ.core :refer [env]]))

(v/defvalidator
  email-domain-validator
  {:default-message-format "email must be from the cyrusinnovation.com domain"}
  [email]
  (= "cyrusinnovation.com" (last (clojure.string/split email #"@"))))

(v/defvalidator
  is-same
  {:default-message-format "is not the same"}
  [value confirm]
  (= value confirm))

(defn- wrap-validation
  "Takes bouncer validation results and returns a vector consisting of a
  boolean indicating if the validation has errors and the same map bouncer
  provides except that bouncer.core/errors is renamed to errors."
  [[errors params]]
  [(nil? errors) (clojure.set/rename-keys params {:bouncer.core/errors :errors})])

(defn validate-email
  "An email is valid if:
       * it is not blank
       * the domain is cyrusinnovation.com"
  [params]
  (wrap-validation
    (b/validate params {:email [v/email email-domain-validator]})))

(defn validate-registration
  "Validates registration information."
  [params]
  (wrap-validation
    (b/validate params
                {:password              v/required
                 :password_confirmation [[is-same (:password params) :message "password confirmation does not match password"]]
                 :location              v/required
                 :latitude              [v/required v/number]
                 :longitude             [v/required v/number]})))
