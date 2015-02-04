(ns lunchlotto.settings.models
  (:require [clojure.java.jdbc :as jdbc]))

(defn delete-user
  "Deletes a user from the database."
  [db id]
  (= [1] (jdbc/delete! db :users ["id=?" id])))
