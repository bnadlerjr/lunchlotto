(ns lunchlotto.common.handlers
  (:require [compojure.core :refer [defroutes]]
            [compojure.route :refer [not-found]]))

(defroutes common-routes
           (not-found "Page not found."))
