(ns lunchlotto.app
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [routes]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [clojure.tools.logging :as log])
  (:require [lunchlotto.migrations :as migrations]
            [lunchlotto.common.handlers :refer [common-routes]]
            [lunchlotto.auth.handlers :refer [auth-routes]]
            [lunchlotto.common.middleware :as middleware]))

(def debug-mode? (env :debug false))

(def application-defaults
  (assoc-in site-defaults [:static :resources] "static"))

(def application
  (wrap-defaults
    (-> (routes auth-routes common-routes)
        middleware/wrap-content-type-html
        middleware/wrap-logger
        (cond->
          debug-mode? wrap-exceptions
          debug-mode? wrap-reload))
    application-defaults))

(defn -main [port]
  (try (migrations/-main (env :database-url))
       (catch Exception e
         (log/error (.getMessage e))))
  (run-server application {:port (Integer. port)})
  (log/info "Server started on port" port)
  (log/info "Debug enabled:" debug-mode?))
