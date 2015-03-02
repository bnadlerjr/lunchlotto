(ns lunchlotto.app
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [selmer.parser :as selmer])
  (:require [lunchlotto.migrations :as migrations]
            [lunchlotto.common.logging :as logging]
            [lunchlotto.common.middleware :as middleware]
            [lunchlotto.routes :refer [application-routes]]

            ; Ensure custom tags are available in templates
            [lunchlotto.common.tags]))

(def debug-mode? (env :debug false))

(def application-defaults
  (assoc-in site-defaults [:static :resources] "static"))

(def application
  (wrap-defaults
    (-> application-routes
        middleware/wrap-coerce-params
        (cond->
          debug-mode? wrap-exceptions
          (not debug-mode?) middleware/wrap-exception-notifier
          debug-mode? wrap-reload)
        middleware/wrap-logger
        middleware/wrap-request-id)
    application-defaults))

(defn -main [port]
  (try (migrations/-main (env :database-url))
       (catch Exception e
         (logging/error (.getMessage e))))
  (selmer/set-resource-path! (clojure.java.io/resource "templates"))
  (run-server application {:port (Integer. port)})
  (logging/info "Server started" {:port port
                                  :debug debug-mode?}))
