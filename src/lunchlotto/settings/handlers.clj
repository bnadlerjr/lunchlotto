(ns lunchlotto.settings.handlers
  (:require [cemerick.friend :as friend]
            [environ.core :refer [env]])
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.settings.models :as models]
            [lunchlotto.settings.views :as views]
            [lunchlotto.common.utils :as utils]))

(def db (env :database-url))

(def t (utils/make-translation-dictionary
         "resources/translations.edn" :en :settings))

(defn show-settings
  [req]
  (let [user (models/find-user-by-id
               db
               (:id (friend/current-authentication req)))]
    (respond-with/ok
      (views/show-settings {:current-user (friend/current-authentication req)
                            :params user}))))

(defn delete-user
  [req]
  (models/delete-user db (:id (friend/current-authentication req)))
  (friend/logout* (respond-with/redirect "/" (t [:flash :deleted]))))
