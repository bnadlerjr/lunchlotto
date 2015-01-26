(ns lunchlotto.app
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [routes]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]))
  (:require [lunchlotto.migrations :as migrations]
            [lunchlotto.common.handlers :refer [common-routes]]
            [lunchlotto.auth.handlers :refer [auth-routes authenticate failed-login]]
            [lunchlotto.lunches.handlers :refer [lunch-routes]]
            [lunchlotto.settings.handlers :refer [settings-routes]]
            [lunchlotto.common.logging :as logging]
            [lunchlotto.common.middleware :as middleware]))

(def debug-mode? (env :debug false))

(def application-defaults
  (assoc-in site-defaults [:static :resources] "static"))

(def application
  (wrap-defaults
    (-> (friend/authenticate
          (routes auth-routes lunch-routes settings-routes common-routes)
          {:allow-anon true
           :login-uri "/login"
           :login-failure-handler failed-login
           :default-landing-uri "/"
           :credential-fn authenticate
           :workflows [(workflows/interactive-form)]})
        middleware/wrap-content-type-html
        middleware/wrap-logger
        middleware/wrap-request-id
        (cond->
          debug-mode? wrap-exceptions
          debug-mode? wrap-reload))
    application-defaults))

(defn -main [port]
  (try (migrations/-main (env :database-url))
       (catch Exception e
         (logging/error (.getMessage e))))
  (run-server application {:port (Integer. port)})
  (logging/info "Server started" {:port port
                                :debug debug-mode?}))
