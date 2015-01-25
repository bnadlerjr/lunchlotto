(ns lunchlotto.migrations
  (:require [clojure.java.jdbc :as jdbc]
            [lunchlotto.common.utils :as utils])
  (:import (java.sql Timestamp)))

(defn initial-schema
  "Initial database schema."
  [db]
  (jdbc/with-db-transaction
    [txn db]

    (jdbc/execute!
      txn
      ["CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\""])

    (jdbc/execute!
      txn
      ["CREATE TABLE users (
          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
          confirmation_token varchar(256) UNIQUE,
          confirmation_token_expires_at TIMESTAMPTZ,
          confirmed_at TIMESTAMPTZ,
          email varchar(254) NOT NULL UNIQUE,
          is_confirmed boolean NOT NULL DEFAULT false,
          latitude double precision,
          location text,
          longitude double precision,
          password varchar(255),
          registered_at TIMESTAMPTZ NOT NULL DEFAULT now()
      )"])
  ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Infrastructure for running migrations.

(defn run-and-record
  "Run the given migration and create an entry for it in the migrations table."
  [db migration]
  (let [name (str (:name (meta migration)))]
    (utils/info "Running migration" {:name name})
    (migration db)
    (jdbc/insert! db :migrations
                  {:name name
                   :created_at (Timestamp. (System/currentTimeMillis))})))

(defn migrate
  "Run the given migrations.

  If the migrations table does not exist, then create it first."
  [db & migrations]
  (jdbc/execute!
    db
    ["CREATE TABLE IF NOT EXISTS migrations
    (name varchar(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)"])

  (utils/info "Checking for migrations")
  (jdbc/with-db-transaction
    [txn db]
    (let [migration-list (jdbc/query txn ["SELECT name FROM migrations"])
          has-run? (set (map :name migration-list))]
      (doseq [m migrations :when (not (has-run? (str (:name (meta m)))))]
        (run-and-record txn m)))))

(defn -main
  "Entry point that runs all database migrations."
  [db]
  (migrate db #'initial-schema))
