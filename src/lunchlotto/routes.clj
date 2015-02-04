(ns lunchlotto.routes
  (:require [lunchlotto.common.handlers :as common]
            [lunchlotto.auth.handlers :as auth]
            [lunchlotto.lunches.handlers :as lunches]
            [lunchlotto.settings.handlers :as settings]
            [lunchlotto.common.responses :as respond-with])
  (:require [compojure.core :refer [ANY DELETE GET POST PUT routes wrap-routes]]
            [compojure.route :refer [not-found]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows])))

(def ^:private anonymous
  (routes
    (GET "/" [] common/home)
    (GET "/register" [] auth/show-registration-page)
    (POST "/register" [] auth/register-user)
    (PUT "/register" [] auth/update-confirmation-token)
    (GET "/confirm" [] auth/show-confirmation-page)
    (POST "/confirm" [] auth/confirm-user)
    (GET "/login" [] auth/show-login-page)
    (ANY "/logout" [] (friend/logout* (respond-with/redirect "/")))))

(def ^:private secure-user
  (wrap-routes
    (routes
      (GET "/lunches/upcoming" [] lunches/show-upcoming)
      (GET "/lunches/pending" [] lunches/show-pending)
      (GET "/lunches/recommended" [] lunches/show-recommended)
      (GET "/settings" [] settings/show-settings)
      (DELETE "/settings" [] settings/delete-user))
    friend/wrap-authorize
    #{:lunchlotto.auth.handlers/user}))

(def application-routes
  (friend/authenticate
    (routes
      anonymous
      secure-user
      (not-found "Page not found."))
    {:login-uri "/login"
     :login-failure-handler auth/failed-login
     :default-landing-uri "/lunches/upcoming"
     :credential-fn auth/authenticate
     :workflows [(workflows/interactive-form)]}))
