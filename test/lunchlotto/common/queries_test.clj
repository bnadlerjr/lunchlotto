(ns lunchlotto.common.queries-test
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc])
  (:require [lunchlotto.common.queries :as q]
            [lunchlotto.migrations :as migration]))

(declare unconfirmed-user unconfirmed-user-with-expired-token confirmed-user)
(declare ^:dynamic *txn*)
(declare setup-db-fixtures! teardown-db-fixtures!)

(def database-url "jdbc:postgresql://localhost/lunchlotto_test")
(def uuid "deadbeef-beef-4666-beef-deaddeaddead")

(use-fixtures
  :once
  (fn [f]
    (migration/-main database-url)
    (setup-db-fixtures!)
    (f)
    (teardown-db-fixtures!)))

(use-fixtures
  :each
  (fn [f]
    (jdbc/with-db-transaction
      [transaction database-url]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))

(deftest find-all-users-test
  (let [users (q/find-all-users {} {:connection *txn*})]
    (is (= 3 (count users)))
    (is (= "jdoe+unconfirmed@example.com" (:email (first users))))))

(deftest find-user-by-id-test
  (testing "found"
    (let [user (q/find-user-by-id {:id uuid} {:connection *txn*})]
      (is (= 1 (count user)))
      (is (= "jdoe+confirmed@example.com" (:email (first user))))))
  (testing "not found"
    (is (= 0 (count (q/find-user-by-id
                      {:id "00000000-0000-4000-8000-000000000000"}
                      {:connection *txn*}))))))

(deftest find-user-by-confirmation-token-test
  (testing "found"
    (let [token "valid-confirmation-token"
          user (q/find-user-by-confirmation-token
                 {:token token}
                 {:connection *txn*})]
      (is (= 1 (count user)))
      (is (= "jdoe+unconfirmed@example.com" (:email (first user))))))
  (testing "token not found"
    (let [token "no-such-confirmation-token"]
      (is (= 0 (count (q/find-user-by-confirmation-token
                        {:token token}
                        {:connection *txn*}))))))
  (testing "token is expired"
    (let [token "expired-confirmation-token"]
      (is (= 0 (count (q/find-user-by-confirmation-token
                        {:token token}
                        {:connection *txn*})))))))

(deftest find-user-by-email-test
  (testing "found"
    (let [user (q/find-user-by-email
                 {:email "jdoe+unconfirmed@example.com"}
                 {:connection *txn*})]
      (is (= 1 (count user)))
      (is (= "jdoe+unconfirmed@example.com" (:email (first user))))))
  (testing "not found"
    (is (= 0 (count (q/find-user-by-email
                      {:email "no-such-email"}
                      {:connection *txn*}))))))

(deftest find-confirmed-user-by-email-test
  (testing "found"
    (let [user (q/find-confirmed-user-by-email
                 {:email "jdoe+confirmed@example.com"}
                 {:connection *txn*})]
      (is (= 1 (count user)))
      (is (= "jdoe+confirmed@example.com" (:email (first user))))))
  (testing "not found"
    (is (= 0 (count (q/find-confirmed-user-by-email
                      {:email "no-such-email"}
                      {:connection *txn*})))))
  (testing "email exists but user unconfirmed"
    (is (= 0
           (count (q/find-confirmed-user-by-email
                    {:email "jdoe+unconfirmed@example.com"}
                    {:connection *txn*}))))))

(let [expires (c/to-timestamp (t/now))
      params {:email                         "john@example.com"
              :confirmation_token            "abc-123"
              :confirmation_token_expires_at expires}]

  (deftest insert-user-success-test
    (is (= 1 (q/insert-user! params {:connection *txn*})))
    (let [user (first (q/find-user-by-email {:email "john@example.com"}
                                            {:connection *txn*}))]
      (is (not (nil? user)))
      (is (= "john@example.com" (:email user)))
      (is (= "abc-123" (:confirmation_token user)))
      (is (= expires (:confirmation_token_expires_at user)))
      (is (= 0
             (t/in-seconds (t/interval (c/from-sql-time (:registered_at user))
                                       (t/now)))))))

  (deftest insert-user-with-null-email-test
    (is (thrown? java.sql.BatchUpdateException
                 (q/insert-user!
                   (assoc params :email nil)
                   {:connection *txn*}))))

  (deftest insert-user-with-duplicate-email-test
    (is
      (thrown? java.sql.BatchUpdateException
               (q/insert-user!
                 (assoc params :email "jdoe+unconfirmed@example.com")
                 {:connection *txn*}))))

  (deftest insert-user-with-duplicate-confirmation-token-test
    (is
      (thrown? java.sql.BatchUpdateException
               (q/insert-user!
                 (assoc params :email       "jane@example.com"
                        :confirmation_token "valid-confirmation-token")
                 {:connection *txn*})))))

(let [params {:location           "some location"
              :latitude           42
              :longitude          -42
              :password           "secret"
              :confirmation_token "valid-confirmation-token"
              :email              "jdoe+unconfirmed@example.com"}]

  (deftest update-user-registration-success-test
    (is (= 1
           (q/update-user-registration! params {:connection *txn*})))
    (let [user (first (jdbc/query *txn* ["SELECT * FROM users WHERE email=?"
                                         (:email params)]))]
      (is (not (nil? user)))
      (is (= "some location" (:location user)))
      (is (= 42.0 (:latitude user)))
      (is (= -42.0 (:longitude user)))
      (is (= "secret" (:password user)))
      (is (true? (:is_confirmed user)))
      (is (= 0
             (t/in-seconds (t/interval (c/from-sql-time (:confirmed_at user))
                                       (t/now)))))
      (is (nil? (:confirmation_token user)))
      (is (nil? (:confirmation_token_expires_at user)))))

  (deftest update-user-registration-confirmation-token-not-found-test
    (is (= 0
           (q/update-user-registration!
             (assoc
               params
               :confirmation_token "no-such-confirmation-token")
             {:connection *txn*}))))

  (deftest update-user-registration-confirmation-token-expired-test
    (is (= 0
           (q/update-user-registration!
             (assoc
               params
               :confirmation_token "expired-confirmation-token")
             {:connection *txn*}))))

  (deftest update-user-registration-email-not-found-test
    (is (= 0
           (q/update-user-registration!
             (assoc params :email "no-such-email")
             {:connection *txn*}))))

  (deftest update-user-registraion-user-already-confirmed-test
    (is (= 0
           (q/update-user-registration!
             (assoc params :confirmation_token nil)
             {:connection *txn*})))))

(let [updated-expires (c/to-timestamp (t/now))
      params {:confirmation_token            "updated-confirmation-token"
              :confirmation_token_expires_at updated-expires
              :email                         "jdoe+unconfirmed@example.com"}]

  (deftest update-user-confirmation-token-success-test
    (let [records-updated (q/update-user-confirmation-token!
                            params
                            {:connection *txn*})
          new-record (first (jdbc/query
                              *txn* ["SELECT * FROM users WHERE email=?"
                                     "jdoe+unconfirmed@example.com"]))]
      (is (= 1 records-updated))
      (is (= "updated-confirmation-token" (:confirmation_token new-record)))
      (is (= updated-expires (:confirmation_token_expires_at new-record)))))

  (deftest update-user-confirmation-token-email-not-found-test
    (is (= 0
           (q/update-user-confirmation-token!
             (assoc params :email "no-such-email")
             {:connection *txn*}))))

  (deftest update-user-confirmation-token-user-already-confirmed-test
    (is (= 0
           (q/update-user-confirmation-token!
             (assoc params :email "jdoe+confirmed@example.com")
             {:connection *txn*})))))

(let [params {:id        uuid
              :location  "A new location"
              :latitude  1
              :longitude 2
              :password  "new-secret"}]
  (deftest update-user-settings-success-test
    (let [records-updated (q/update-user-settings!
                            params
                            {:connection *txn*})
          updated-user (first (jdbc/query
                                *txn* ["SELECT * FROM users WHERE id=?::uuid"
                                       uuid]))]
      (is (= 1 records-updated))
      (is (= "A new location" (:location updated-user)))
      (is (= 1.0 (:latitude updated-user)))
      (is (= 2.0 (:longitude updated-user)))
      (is (= "new-secret" (:password updated-user)))))

  (deftest update-user-settings-without-new-password-test
    (let [records-updated (q/update-user-settings!
                            (assoc params :password "")
                            {:connection *txn*})
          updated-user (first (jdbc/query
                                *txn* ["SELECT * FROM users WHERE id=?::uuid"
                                       uuid]))]
      (is (= 1 records-updated))
      (is (= "A new location" (:location updated-user)))
      (is (= 1.0 (:latitude updated-user)))
      (is (= 2.0 (:longitude updated-user)))
      (is (= "secret" (:password updated-user))))))

(deftest delete-user-success-test
  (is (= 1 (q/delete-user! {:id uuid} {:connection *txn*})))
  (is (= 2 (count (jdbc/query *txn* ["SELECT * FROM users"])))))

(deftest delete-user-user-id-not-found-test
  (is (= 0
         (q/delete-user! {:id "00000000-0000-4000-8000-000000000000"}
                         {:connection *txn*})))
  (is (= 3 (count (jdbc/query *txn* ["SELECT * FROM users"])))))

(def unconfirmed-user
  {:email                         "jdoe+unconfirmed@example.com"
   :password                      "secret"
   :confirmation_token            "valid-confirmation-token"
   :confirmation_token_expires_at (c/to-timestamp
                                    (t/plus (t/now) (t/minutes 30)))})

(def unconfirmed-user-with-expired-token
  {:email                         "jdoe+unconfirmed+expired@example.com"
   :password                      "secret"
   :confirmation_token            "expired-confirmation-token"
   :confirmation_token_expires_at (c/to-timestamp
                                    (t/minus (t/now) (t/minutes 1)))})

(def confirmed-user
  {:id            (java.util.UUID/fromString uuid)
   :email         "jdoe+confirmed@example.com"
   :password      "secret"
   :confirmed_at  (c/to-timestamp (t/now))
   :registered_at (c/to-timestamp (t/now))
   :is_confirmed  true
   :location      "85 5th Avenue, Manhattan, NY, United States"
   :latitude      40.7372676
   :longitude     -73.9920646})

(defn setup-db-fixtures!
  []
  (jdbc/insert!
    database-url
    :users
    unconfirmed-user
    unconfirmed-user-with-expired-token
    confirmed-user))

(defn teardown-db-fixtures!
  []
  (jdbc/execute! database-url ["DROP TABLE users"])
  (jdbc/execute! database-url ["DROP TABLE migrations"]))
