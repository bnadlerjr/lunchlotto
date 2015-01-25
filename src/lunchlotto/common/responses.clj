(ns lunchlotto.common.responses
  (:require [ring.util.response :as ring]))

(defn ok
  "Returns a 200 response with the given body and no headers."
  [body]
  (ring/response body))

(defn bad-request
  "Returns a ring response with the given body, a status code of 400, and no
  headers."
  [body]
  {:status  400
   :headers {}
   :body    body})

(defn redirect
  "Redirects to specified URL with an optional flash message."
  ([url]
    (ring/redirect url))
  ([url flash]
    (assoc (ring/redirect url) :flash flash)))
