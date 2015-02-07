(ns lunchlotto.settings.models
  (:require [clojure.java.jdbc :as jdbc])
  (:require [lunchlotto.auth.utils :as utils]))

(defn delete-user
  "Deletes a user from the database."
  [db id]
  (= [1] (jdbc/delete! db :users ["id=?" id])))

(defn find-user-by-id
  "Finds a user by their ID. Returns nil if a user cannot be found."
  [db id]
  (first (jdbc/query db ["SELECT * FROM users WHERE id=?" id])))

(defn extract-settings-to-update
  [params]
  (let [settings (select-keys params [:location :latitude :longitude])]
    (if (contains? params :new_password)
      (merge settings {:password (utils/encrypt-password (:new_password params))})
      settings)))

(defn update-settings
  "Updates a user's settings."
   [db params]
   (= [1] (jdbc/update! db :users (extract-settings-to-update params) ["id=?::uuid" (:id params)])))
