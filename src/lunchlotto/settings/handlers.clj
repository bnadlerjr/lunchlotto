(ns lunchlotto.settings.handlers
  (:require [cemerick.friend :as friend]
            [environ.core :refer [env]])
  (:require [lunchlotto.common.responses :as response]
            [lunchlotto.settings.models :as models]
            [lunchlotto.settings.validations :as val]
            [lunchlotto.common.utils :as utils]
            [lunchlotto.auth.utils :as auth-utils]))

(def db (env :database-url))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :settings))

(defn show-settings
  [req]
  (let [user (models/find-user-by-id
               db
               (:id (friend/current-authentication req)))]
    (response/render :ok [:settings :show] {:user user})))

(defn update-settings
  [req]
  (let [[valid? data] (val/validate-settings (:params req))
        user (models/find-user-by-id db (:id data))
        response-context (assoc data :email (:email user))]

    (cond (not valid?)
          (response/render :bad-request [:settings :show] {:user response-context})

          (and user
               (auth-utils/check-password (:current_password data) (:password user)))
          (do
            (models/update-settings db data)
            (response/redirect "/settings" (t [:flash :updated])))

          :else
          (response/render :bad-request [:settings :show] {:user (assoc-in response-context [:errors] {:current_password [(t [:flash :invalid_current_password])]})}))))

(defn delete-user
  [req]
  (models/delete-user db (:id (friend/current-authentication req)))
  (friend/logout* (response/redirect "/" (t [:flash :deleted]))))
