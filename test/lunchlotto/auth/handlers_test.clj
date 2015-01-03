(ns lunchlotto.auth.handlers-test
  (:require [clojure.test :refer :all]
            [lunchlotto.auth.handlers :as handlers]
            [lunchlotto.auth.models :as models]))

(defn contains
  "Returns true if str contains substr, otherwise false."
  [str substr]
  (boolean (re-find (re-pattern substr) str)))

(deftest show-registration-page
  (testing "successfully render registration page"
    (let [resp (handlers/show-registration-page {})]
      (is (= 200 (:status resp)))
      (is (contains (:body resp) "Register New User")))))

(deftest register-user
  (testing "valid email format and email is not in database"
    (with-redefs [models/find-user-by-email (fn [_ _] nil)
                  models/register-user (fn [_ _] "some-token")]
      (let [resp (handlers/register-user {:email "jdoe@cyrusinnovation.com"})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (contains (:flash resp) "You've just been an email that contains a confirmation link.")))))

  (testing "valid email format and confirmed user is in database"
    (with-redefs [models/find-user-by-email (fn [_ _] {:is_confirmed true})]
      (let [resp (handlers/register-user {:email "jdoe@cyrusinnovation.com"})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (= "That email has already been used. Please login." (:flash resp))))))

  (testing "valid email format and unconfirmed user is in database"
    (with-redefs [models/find-user-by-email (fn [_ _] {:is_confirmed false})]
      (let [resp (handlers/register-user {:email "jdoe@cyrusinnovation.com"})]
        (is (= 200 (:status resp)))
        (is (contains (:body resp) "Re-send Confirmation Email")))))

  (testing "invalid email format"
    (let [resp (handlers/register-user {:email "foo@example.com"})]
      (is (= 400 (:status resp)))
      (is (contains (:body resp) "Register New User"))
      (is (contains (:body resp) "email must be from the cyrusinnovation.com domain")))))

(deftest show-confirmation-page
  (testing "with valid confirmation token"
    (with-redefs [models/find-user-by-confirmation-token (fn [_ _] {})]
      (let [resp (handlers/show-confirmation-page {})]
        (is (= 200 (:status resp)))
        (is (contains (:body resp) "Finish Registration")))))

  (testing "with invalid confirmation token"
    (with-redefs [models/find-user-by-confirmation-token (fn [_ _] nil)]
      (let [resp (handlers/show-confirmation-page {})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (= "Invalid confirmation token." (:flash resp)))))))

(deftest update-confirmation-token
  (testing "successfully updated token"
    (with-redefs [models/update-confirmation-token (fn [_ _] "some-token")]
      (let [resp (handlers/update-confirmation-token {:params {:email "jdoe@cyrusinnovation.com"}})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (contains (:flash resp) "You've just been an email that contains a confirmation link.")))))

  (testing "invalid email format"
    (let [resp (handlers/update-confirmation-token {:params {:email "foo@example.com"}})]
      (is (= 400 (:status resp)))
      (is (contains (:body resp) "Re-send Confirmation Email"))
      (is (contains (:body resp) "email must be from the cyrusinnovation.com domain")))))

(deftest confirm-user
  (let [valid-params {:password              "secret"
                      :password_confirmation "secret"
                      :location              "Somewhere"
                      :latitude              "42.123"
                      :longitude             "-42.123"}]
    (testing "successfully confirm user"
      (with-redefs [models/confirm-user (fn [_ _] nil)]
        (let [resp (handlers/confirm-user valid-params)]
          (is (= 302 (:status resp)))
          (is (= {"Location" "/"} (:headers resp)))
          (is (= "Thanks for confirming your email. You are now fully registered." (:flash resp))))))

    (testing "latitude is not number"
      (let [resp (handlers/confirm-user (assoc valid-params :latitude "not a number"))]
        (is (= 400 (:status resp)))))

    (testing "longitude is not a number"
      (let [resp (handlers/confirm-user (assoc valid-params :longitude "not a number"))]
        (is (= 400 (:status resp)))))

    (testing "any other validation error"
      (let [resp (handlers/confirm-user (assoc valid-params :location nil))]
        (is (= 400 (:status resp)))
        (is (contains (:body resp) "location must be present"))))))
