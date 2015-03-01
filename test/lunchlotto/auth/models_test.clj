(ns lunchlotto.auth.models-test
  (:require [clojure.test :refer :all]
            [clojure.java.jdbc :as jdbc]
            [environ.core :refer [env]]
            [clj-time.core :as time]
            [clj-time.coerce :as coerce])
  (:require [lunchlotto.auth.models :as models]
            [lunchlotto.migrations :as migration]
            [lunchlotto.auth.utils :as utils])
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

(deftest register-user
  (let [expected-token "abc123"
        expected-email "jdoe@example.com"
        expected-expiry (coerce/to-timestamp (time/now))]
    (with-redefs [utils/generate-token (fn [] [expected-token expected-expiry])]
      (let [token (models/register-user *txn* expected-email)
            user (models/find-user-by-email *txn* expected-email)]

        (testing "returns confirmation token"
          (is (= expected-token token)))
        (testing "stores email address"
          (is (= expected-email (:email user))))
        (testing "stores hashed value of registration token"
          (is (= (utils/digest token) (:confirmation_token user))))
        (testing "stores the registration token expiration timestamp"
          (is (= expected-expiry (:confirmation_token_expires_at user))))
        (testing "raises exception if email is not unique"
          (is (thrown? PSQLException (models/register-user *txn* expected-email))))))))

(deftest find-user-by-email
  (let [[_ user] (create-unconfirmed-test-user)]

    (testing "successfully found user"
      (is (= user (models/find-user-by-email *txn* (:email user)))))
    (testing "user not found"
      (is (nil? (models/find-user-by-email *txn* "not-a-user@example.com"))))))

(deftest find-user-by-confirmation-token
  (let [[token user] (create-unconfirmed-test-user)]

    (testing "successfully find user using raw token"
      (is (= user (models/find-user-by-confirmation-token *txn* token))))
    (testing "user not found"
      (is (nil? (models/find-user-by-confirmation-token *txn* "invalid-token"))))
    (testing "expired token"
      (let [[expired-token _] (create-unconfirmed-test-user-with-expired-token)]
        (is (nil? (models/find-user-by-confirmation-token *txn* expired-token)))))))

(deftest confirm-user
  (let [[token user] (create-unconfirmed-test-user)
        result (models/confirm-user *txn* {:location "123 Elm Street"
                                           :latitude 42.123
                                           :longitude -42.321
                                           :password "secret"
                                           :confirmation_token token
                                           :email (:email user)})
        confirmed-user (models/find-user-by-email *txn* (:email user))]

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

(deftest update-confirmation-token
  (let [[token user] (create-unconfirmed-test-user)]

    (testing "successful update"
      (let [new-token (models/update-confirmation-token *txn* "jdoe@example.com")
            updated-user (models/find-user-by-email *txn* "jdoe@example.com")]
        (testing "returns updated token"
          (is (not (= token new-token))))
        (testing "updates token"
          (is (= (utils/digest new-token) (:confirmation_token updated-user))))
        (testing "resets expiry time"
          (is (not (= (:confirmation_token_expires_at user)
                      (:confirmation_token_expires_at updated-user)))))))

    (testing "user is already confirmed"
      (let [user (create-confirmed-test-user)
            result (models/update-confirmation-token *txn* (:email user))
            unchanged-user (models/find-user-by-email *txn* (:email user))]
        (testing "returns nil"
          (is (nil? result)))
        (testing "does not change token"
          (is (nil? (:confirmation_token unchanged-user))))
        (testing "does not change expiry"
          (is (nil? (:confirmation_token_expires_at unchanged-user))))))

    (testing "email cannot be found"
      (is (nil? (models/update-confirmation-token *txn* "no-such-email@example.com"))))))

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
