(ns lunchlotto.adapters.postmark
  (:require [postmark.core :as pm])
  (:require [lunchlotto.protocols :as protocols]
            [lunchlotto.common.logging :as logging]))

(defrecord Postmark
  [client]
  protocols/Email
  (send-confirmation-token [this email link]
    (logging/debug (str "Sending confirmation email with link:" link))
    (client {:to      email
             :subject "[LunchLotto] Confirm Email Address"
             :text    (format "Thanks for registering with LunchLotto! Please confirm your email address by clicking this link: %s" link)})))
