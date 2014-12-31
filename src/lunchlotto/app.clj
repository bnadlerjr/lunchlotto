(ns lunchlotto.app
  (:require [org.httpkit.server :refer [run-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [routes]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]])
  (:require [lunchlotto.common.handlers :refer [common-routes]]
            [lunchlotto.common.middleware :refer [wrap-simulated-methods]]))

(def debug-mode? (env :debug false))

(def application-defaults
  (assoc-in site-defaults [:static :resources] "static"))

(def application
  (wrap-defaults
    (cond->
      (routes common-routes)
      wrap-simulated-methods
      debug-mode? wrap-exceptions
      debug-mode? wrap-reload)
    application-defaults))

(defn -main [port]
  (run-server application {:port (Integer. port)})
  (println (str "Server started on port " port))
  (println (str "Debug enabled: " debug-mode?)))
