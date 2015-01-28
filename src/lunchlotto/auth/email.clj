(ns lunchlotto.auth.email
  (:require [lunchlotto.common.utils :as utils]
            [lunchlotto.common.logging :as logging]))

(defn- build-confirmation-link
  [token {:keys [:scheme :server-name :server-port]}]
  (let [base-url (str (name scheme) "://" server-name)
        uri (str "/confirm?confirmation_token=" token)]
    (if (= 8080 server-port)
      (str base-url uri)
      (str base-url ":" server-port uri))))

(def send-confirmation-email
  "Returns a function that sends a user a confirmation email."
  (fn
    [email token req]
    (logging/debug (str "Sending confirmation email with token:" token))
    (utils/make-email {:to email
                       :subject "[LunchLotto] Confirm Email Address"
                       :text (format "Thanks for registering with LunchLotto! Please confirm your email address by clicking this link: %s"
                                     (build-confirmation-link token req))})))
