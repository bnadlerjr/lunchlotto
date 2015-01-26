(ns lunchlotto.routes
  (:require [lunchlotto.common.handlers :as common]
            [lunchlotto.auth.handlers :as auth]
            [lunchlotto.lunches.handlers :as lunches]
            [lunchlotto.settings.handlers :as settings])
  (:require [compojure.core :refer [GET POST PUT routes]]
            [compojure.route :refer [not-found]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows])))

(def application-routes
  (friend/authenticate
    (routes
      (GET "/" [] common/home)

      (GET "/register" [] auth/show-registration-page)
      (POST "/register" {params :params} (auth/register-user params))
      (PUT "/register" [] auth/update-confirmation-token)

      (GET "/confirm" [] auth/show-confirmation-page)
      (POST "/confirm" {params :params} (auth/confirm-user params))

      (GET "/login" [] auth/show-login-page)

      (GET "/lunches/upcoming" [] lunches/show-upcoming)
      (GET "/lunches/pending" [] lunches/show-pending)
      (GET "/lunches/recommended" [] lunches/show-recommended)

      (GET "/settings" [] settings/show-settings)
      (not-found "Page not found."))
    {:allow-anon true
     :login-uri "/login"
     :login-failure-handler auth/failed-login
     :default-landing-uri "/"
     :credential-fn auth/authenticate
     :workflows [(workflows/interactive-form)]}))
