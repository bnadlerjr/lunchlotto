(ns lunchlotto.settings.handlers
  (:require [cemerick.friend :as friend]
            [environ.core :refer [env]])
  (:require [lunchlotto.common.responses :as respond-with]
            [lunchlotto.settings.models :as models]
            [lunchlotto.settings.views :as views]))

(def db (env :database-url))

(defn show-settings
  [req]
  (respond-with/ok (views/show-settings {:current-user (friend/current-authentication req)
                                         :params (friend/current-authentication req)})))

(defn delete-user
  [req]
  (models/delete-user db (:id (friend/current-authentication req)))
  (friend/logout* (respond-with/redirect "/" "Your account has been deleted.")))
