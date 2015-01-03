(ns lunchlotto.common.handlers
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [not-found]]
            [ring.util.response :as ring])
  (:require [lunchlotto.common.views :as views]))

(defn home
  "The home page handler."
  [req]
  (ring/response (views/home-page (req :flash))))

(defroutes common-routes
           (GET "/" [] home)
           (not-found "Page not found."))
