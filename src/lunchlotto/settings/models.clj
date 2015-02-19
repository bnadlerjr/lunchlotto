(ns lunchlotto.settings.models
  (:require [clojure.java.jdbc :as jdbc]))

(defn delete-user
  "Deletes a user from the database."
  [db id]
  (= [1] (jdbc/delete! db :users ["id=?" id])))

(defn find-user-by-id
  "Finds a user by their ID. Returns nil if a user cannot be found."
  [db id]
  (first (jdbc/query db ["SELECT * FROM users WHERE id=?" id])))
