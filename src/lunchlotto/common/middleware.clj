(ns lunchlotto.common.middleware
  (:require [ring.util.response :as ring]))

(def sim-methods {"PUT" :put "DELETE" :delete})

(defn wrap-simulated-methods
  "Since browsers do not support PUT and DELETE, override the request method if
  the request contains a param named _method."
  [hdlr]
  (fn [req]
    (if-let [method (and (= :post (:request-method req))
                         (sim-methods (get-in req [:params "_method"])))]
      (hdlr (assoc req :request-method method))
      (hdlr req))))

(defn wrap-content-type-html
  "The Ring defaults library provides a middleware for wrapping the content
  type, however, it bases it's decision on the file type. Since Hiccup is being
  used, no file type is given so the default is 'application/octet-stream'.
  This middleware explicitly sets the content type to be
  'text/html; charset=utf-8' instead."
  [hdlr]
  (fn [req]
    (let [resp (hdlr req)]
      (ring/content-type resp "text/html; charset=utf-8"))))
