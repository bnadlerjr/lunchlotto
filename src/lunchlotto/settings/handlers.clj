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
  (let [user (models/find-user-by-id db (:id (friend/current-authentication req)))
        [valid? data] (val/validate-settings (:params req) (:password user))]

    (if valid?
      (do
        (models/update-settings db data)
        (response/redirect "/settings" (t [:flash :updated])))
      (response/render :bad-request [:settings :show]
                       {:user (assoc data :email (:email user))}))))

(defn delete-user
  [req]
  (models/delete-user db (:id (friend/current-authentication req)))
  (friend/logout* (response/redirect "/" (t [:flash :deleted]))))
