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
  [email]
  (first (jdbc/insert! *txn* :users {:email email
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
    (let [user (create-test-user "jdoe@example.com")]
      (is (= {:count 1} (get-user-count)))
      (is (true? (models/delete-user *txn* (:id user))))
      (is (= {:count 0} (get-user-count)))))
  (testing "attempting to delete a user that doesn't exist"
    (let [user (create-test-user "jdoe@example.com")]
      (is (= {:count 1} (get-user-count)))
      (is (false? (models/delete-user *txn* (java.util.UUID/randomUUID))))
      (is (= {:count 1} (get-user-count))))))

(deftest find-user-by-id
  (let [user (create-test-user "jdoe@example.com")]
    (testing "successfully find user"
      (is (= {:count 1} (get-user-count)))
      (is (= user (models/find-user-by-id *txn* (:id user)))))
    (testing "user not found"
      (is (= {:count 1} (get-user-count)))
      (is (nil? (models/find-user-by-id *txn* (java.util.UUID/randomUUID)))))))

(deftest update-settings
  (testing "successfully update password only"
    (let [user (create-test-user "jdoe@example.com")
          params {:id (:id user) :new_password "supersecret"}]
      (is (true? (models/update-settings *txn* params)))
      (let [updated-user (models/find-user-by-id *txn* (:id user))]
        (is (true? (auth-utils/check-password "supersecret" (:password updated-user))))
        (is (= "Somewhere" (:location updated-user)))
        (is (= 42.123 (:latitude updated-user)))
        (is (= -42.321 (:longitude updated-user))))))

  (testing "successfully update location info only"
    (let [user (create-test-user "jake@example.com")
          params {:id (:id user) :location "Anywhere" :latitude 10.1 :longitude 20.2}]
      (is (true? (models/update-settings *txn* params)))
      (let [updated-user (models/find-user-by-id *txn* (:id user))]
        (is (true? (auth-utils/check-password "secret" (:password updated-user))))
        (is (= "Anywhere" (:location updated-user)))
        (is (= 10.1 (:latitude updated-user)))
        (is (= 20.2 (:longitude updated-user))))))

  (testing "successfully update password and location info"
    (let [user (create-test-user "jane@example.com")
          params {:id (:id user) :new_password "supersecret" :location "Here" :latitude 1.1 :longitude 2.2}]
      (is (true? (models/update-settings *txn* params)))
      (let [updated-user (models/find-user-by-id *txn* (:id user))]
        (is (true? (auth-utils/check-password "supersecret" (:password updated-user))))
        (is (= "Here" (:location updated-user)))
        (is (= 1.1 (:latitude updated-user)))
        (is (= 2.2 (:longitude updated-user)))))

  (testing "cannot update non-existent user"
    (let [params {:id (:id (java.util.UUID/randomUUID)) :new_password "foo"}]
      (is (false? (models/update-settings *txn* params)))))))
