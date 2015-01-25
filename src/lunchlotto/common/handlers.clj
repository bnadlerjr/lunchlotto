(ns lunchlotto.common.handlers
  (:require [compojure.core :refer [routes GET]]
            [compojure.route :refer [not-found]]
            [ring.util.response :as ring])
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.common.views :as views]))

(defn home
  "The home page handler."
  [req]
  (respond-with/ok (views/home-page (req :flash))))

(def common-routes
  (routes
    (GET "/" [] home)
    (not-found "Page not found.")))
