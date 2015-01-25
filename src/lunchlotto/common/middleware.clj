(ns lunchlotto.common.middleware
  (:require [clojure.string :as s]
            [ring.util.response :as ring]
            [lunchlotto.common.logging :as logging])
  (:import (java.util UUID)))

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

(defn wrap-logger
  "Logs requests using clojure.tools.logging."
  [hdlr]
  (fn [req]
    (let [start (System/currentTimeMillis)
          req-id (get-in req [:headers "x-request-id"])
          req-attrs {:method (s/upper-case (name (:request-method req)))
                     :url (str (:uri req)
                               (if-let [query-string (:query-string req)]
                                 (str "?" query-string)))
                     :params (:params req)
                     :host (:server-name req)
                     :protocol (s/upper-case (name (:scheme req)))
                     :at "info"
                     :request_id req-id}]

      (logging/info "Starting Ring request" req-attrs)
      (let [resp (hdlr req)
            resp-attrs {:status (:status resp)
                        :msg "Finished Ring request"
                        :duration (str (- (System/currentTimeMillis) start) "ms")
                        :request_id req-id}]
        (logging/info "Finished Ring request"
             (if-let [location (get-in resp [:headers "Location"])]
               (assoc resp-attrs :location location)
               resp-attrs))
        resp))))

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

(defn wrap-request-id
  "Adds a 'X-Request-ID header to the request.

  When deployed on Heroku, the Heroku router generates a unique Request ID for
  every request it receives. This ID is passed as an 'X-Request-ID' header.
  This middleware adds the 'X-Request-ID' header if it is not present so that
  Request ID's are available when deployed elsewhere (i.e. local development).
  See https://devcenter.heroku.com/articles/http-request-id for more
  information on Request ID's."
  [hdlr]
  (fn [req]
    (if (get-in req [:headers "x-request-id"])
      (hdlr req)
      (hdlr (assoc-in req [:headers "x-request-id"] (str (UUID/randomUUID)))))))
