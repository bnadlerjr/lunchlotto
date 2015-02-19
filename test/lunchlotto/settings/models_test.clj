(ns lunchlotto.settings.models-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]])
  (:require [lunchlotto.settings.models :as models]
            [lunchlotto.migrations :as migration]
            [lunchlotto.auth.utils :as auth-utils]))

(declare ^:dynamic *txn*)

(use-fixtures
  :once
  (fn [f]
    (migration/-main (env :database-url)) (f)))

(use-fixtures
  :each
  (fn [f]
    (jdbc/with-db-transaction
      [transaction (env :database-url)]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))

(defn- create-test-user
  []
  (first (jdbc/insert! *txn* :users {:email "jdoe@example.com"
                                     :password (auth-utils/encrypt-password "secret")
                                     :is_confirmed true
                                     :location "Somewhere"
                                     :latitude 42.123
                                     :longitude -42.321})))

(defn- get-user-count
  []
  (first (jdbc/query *txn* "SELECT COUNT(*) FROM users")))

(deftest delete-user
  (testing "successfully delete user"
    (let [user (create-test-user)]
      (is (= {:count 1} (get-user-count)))
      (is (true? (models/delete-user *txn* (:id user))))
      (is (= {:count 0} (get-user-count)))))
  (testing "attempting to delete a user that doesn't exist"
    (let [user (create-test-user)]
      (is (= {:count 1} (get-user-count)))
      (is (false? (models/delete-user *txn* (java.util.UUID/randomUUID))))
      (is (= {:count 1} (get-user-count))))))
