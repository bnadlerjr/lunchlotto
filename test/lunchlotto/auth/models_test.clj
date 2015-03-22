(ns lunchlotto.auth.models-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [lunchlotto.test-helpers :refer :all])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.migrations :as migration]
            [lunchlotto.auth.utils :as utils]
            [lunchlotto.common.queries :as q])
  (:import (org.postgresql.util PSQLException)))

(declare ^:dynamic *txn*)

(def database-url "jdbc:postgresql://localhost/lunchlotto_test")

(use-fixtures
  :once
  (fn [f]
    (migration/-main database-url) (f)))

(use-fixtures
  :each
  (fn [f]
    (jdbc/with-db-transaction
      [transaction database-url]
      (jdbc/db-set-rollback-only! transaction)
      (binding [*txn* transaction] (f)))))

(defn- create-test-user
  [params]
  (first (jdbc/insert! *txn* :users params)))

(defn- create-unconfirmed-test-user
  []
  (let [[token expires] (utils/generate-token)]
    [token (create-test-user {:email                         "jdoe@example.com"
                              :confirmation_token            (utils/digest token)
                              :confirmation_token_expires_at expires})]))

(defn- create-unconfirmed-test-user-with-expired-token
  []
  (let [[token _] (utils/generate-token)
        expires (coerce/to-timestamp (time/minus (time/now) (time/minutes 1)))]
    [token (create-test-user {:email                         "jdoe-expired@example.com"
                              :confirmation_token            (utils/digest token)
                              :confirmation_token_expires_at expires})]))

(defn- create-confirmed-test-user
  []
  (create-test-user {:email "jdoe-confirmed@example.com"
                     :password (utils/encrypt-password "secret")
                     :is_confirmed true}))

(defmocktest register-user-test
  (stubbing [utils/generate-token ["00000000-0000-4000-8000-000000000000"
                                   #inst "2015-03-14T21:06:14.064-00:00"]]
    (mocking [q/insert-user!]
      (let [token (models/register-user "jdoe@example.com")]
        (testing "returns the raw confirmation token"
          (is (= "00000000-0000-4000-8000-000000000000" token)))
        (testing "stores the correct data"
          (verify-call-times-for q/insert-user! 1)
          (verify-first-call-args-for
            q/insert-user!
            {:email                         "jdoe@example.com"
             :confirmation_token            "01e302b749e7ad2b46ba86e5ebd7c4e67580d94cc5b7bc1f96eb6e7e236f3b8ecf16a162f7359941bf25ec881b69418c777f4fe43cd91a95fa3286ad8e1d494d"
             :confirmation_token_expires_at #inst "2015-03-14T21:06:14.064-00:00"}))))))

(defmocktest find-user-by-email-test
  (testing "successfully found user"
    (stubbing [q/find-user-by-email [{:id "abc123"}]]
      (is (= {:id "abc123"} (models/find-user-by-email "jdoe@example.com")))))

  (testing "user not found"
    (stubbing [q/find-user-by-email []]
      (is (nil? (models/find-user-by-email "jdoe@example.com"))))))

(defmocktest find-user-by-confirmation-token-test
  (testing "returns user map"
    (stubbing [q/find-user-by-confirmation-token [{:id "abc123"}]]
      (is (= {:id "abc123"} (models/find-user-by-confirmation-token "some-token")))))
  (testing "digests token before querying"
    (mocking [q/find-user-by-confirmation-token]
      (models/find-user-by-confirmation-token "some-token")
      (verify-call-times-for q/find-user-by-confirmation-token 1)
      (verify-first-call-args-for q/find-user-by-confirmation-token {:token "49e52e9fa1d1b3947356ff1c9e32b002bbd0dbf7437afc1f7b68b160fa034d7fbb627d1568f428e3c46d5c0849bc5d2a00f916c9dfd255a1166fdb431f1d2372"})))
  (testing "return nil if user not found"
    (stubbing [q/find-user-by-confirmation-token []]
      (is (nil? (models/find-user-by-confirmation-token "some-token"))))))

(deftest confirm-user
  (let [[token user] (create-unconfirmed-test-user)
        result (models/confirm-user *txn* {:location "123 Elm Street"
                                           :latitude 42.123
                                           :longitude -42.321
                                           :password "secret"
                                           :confirmation_token token
                                           :email (:email user)})
        confirmed-user (first (jdbc/query *txn* ["SELECT * FROM users WHERE email=?" (:email user)]))]

    (testing "returns true when confirmation is successful"
      (is (true? result)))
    (testing "sets location information"
      (is (= "123 Elm Street" (:location confirmed-user)))
      (is (= 42.123 (:latitude confirmed-user)))
      (is (= -42.321 (:longitude confirmed-user))))
    (testing "encrypts password"
      (is (true? (utils/check-password "secret" (:password confirmed-user)))))
    (testing "sets is_confirmed flag"
      (is (true? (:is_confirmed confirmed-user))))
    (testing "sets confirmation timestamp"
      (is (not (nil? (:confirmed_at confirmed-user)))))
    (testing "sets confirmation token to null"
      (is (nil? (:confirmation_token confirmed-user))))
    (testing "sets confirmation token expiry to null"
      (is (nil? (:confirmation_token_expires_at confirmed-user))))))

(defmocktest update-confirmation-token-test
  (testing "successful update"
    (stubbing [utils/generate-token ["abc123" "some date"]]
      (mocking [q/update-user-confirmation-token!]
        (let [token (models/update-confirmation-token "jdoe@example.com")]
          (verify-call-times-for q/update-user-confirmation-token! 1)
          (verify-first-call-args-for
            q/update-user-confirmation-token!
            {:confirmation_token "c70b5dd9ebfb6f51d09d4132b7170c9d20750a7852f00680f65658f0310e810056e6763c34c9a00b0e940076f54495c169fc2302cceb312039271c43469507dc"
             :confirmation_token_expires_at "some date"
             :email "jdoe@example.com"})))))

  (testing "failed update"
    (stubbing [q/update-user-confirmation-token! [0]]
      (is (nil? (models/update-confirmation-token "jdoe@example.com"))))))

(deftest authenticate-user
  (let [confirmed-user (create-confirmed-test-user)
        unconfirmed-user (create-unconfirmed-test-user)]
    (testing "successfully authenticate confirmed user"
      (is (= confirmed-user (models/authenticate-user *txn* (:email confirmed-user) "secret"))))
    (testing "unconfirmed users cannot be authenticated"
      (is (nil? (models/authenticate-user *txn* (:email unconfirmed-user) "secret"))))
    (testing "bad email"
      (is (nil? (models/authenticate-user *txn* "bademail@example.com" "secret"))))
    (testing "bad password"
      (is (nil? (models/authenticate-user *txn* (:email confirmed-user) "invalid"))))))
