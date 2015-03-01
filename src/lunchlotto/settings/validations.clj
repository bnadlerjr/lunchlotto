(ns lunchlotto.settings.validations
  (:require [bouncer.core :as bouncer]
            [bouncer.validators :as validators])
  (:require [lunchlotto.auth.utils :as auth-utils]
            [lunchlotto.common.utils :as utils]))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :auth :validations))

(defn- wrap-validation
  "Takes bouncer validation results and returns a vector consisting of a
  boolean indicating if the validation has errors and the same map bouncer
  provides except that bouncer.core/errors is renamed to errors."
  [[errors params]]
  [(nil? errors)
   (clojure.set/rename-keys params {:bouncer.core/errors :errors})])

(validators/defvalidator
  is-same
  {:default-message-format "is not the same"}
  [value confirm]
  (= value confirm))

(validators/defvalidator
  correct-password
  {:default-message-format "current password is invalid"}
  [value password]
  (auth-utils/check-password value password))

(defn validate-settings
  "Validates user settings."
  [params current-password]
  (wrap-validation
    (bouncer/validate
      params
      {:current_password          [validators/required
                                   [correct-password current-password]]
       :new_password_confirmation [[is-same (:new_password params)
                                    :message (t [:password :mismatch])]]
       :location                  validators/required
       :latitude                  [validators/required validators/number]
       :longitude                 [validators/required validators/number]})))
