(ns lunchlotto.common.responses
  (:require [cemerick.friend :as friend]
            [compojure.response :refer [Renderable]]
            [ring.util.response :as ring]
            [selmer.parser :as selmer]))

(deftype SelmerPage
  [status template params]
  Renderable
  (render [_ request]
    {:status  status
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body    (->> (assoc params
                          :current-auth (friend/current-authentication request)
                          :flash        (:flash request))
                   (selmer/render-file template))}))

(def status-codes {:ok          200
                   :bad-request 400})

(defn- find-template
  [[scope filename]]
  (str
    (java.nio.file.Paths/get
      "lunchlotto"
      (into-array [(name scope) "templates" (str (name filename) ".html")]))))

(defn render
  "Render a Selmer template."
  [status template & [params]]
  (SelmerPage. (status status-codes) (find-template template) params))

(defn redirect
  "Redirects to specified URL with an optional flash message."
  ([url]
   {:status  302
    :headers {"Content-Type" "text/html; charset=utf-8" "Location" url}
    :body    ""})
  ([url flash]
    (assoc (redirect url) :flash flash)))
