(ns lunchlotto.auth.handlers-test
  (:require [clojure.test :refer :all]
            [lunchlotto.auth.handlers :as handlers]
            [lunchlotto.auth.models :as models]
            [lunchlotto.auth.email :as email]))

(defn contains
  "Returns true if str contains substr, otherwise false."
  [str substr]
  (boolean (re-find (re-pattern substr) str)))

(deftest show-registration-page
  (testing "successfully render registration page"
    (let [resp (handlers/show-registration-page {})]
      (is (= 200 (:status resp)))
      (is (contains (:body resp) "Register New User")))))

(deftest show-login-page
  (testing "successfully render login page"
    (let [resp (handlers/show-login-page {})]
      (is (= 200 (:status resp)))
      (is (contains (:body resp) "Login")))))

(deftest failed-login
  (testing "successfully render failed login page"
    (let [resp (handlers/failed-login {})]
      (is (= 400 (:status resp)))
      (is (contains (:body resp) "Sorry, that email and password combination is incorrect.")))))

(deftest register-user
  (testing "valid email format and email is not in database"
    (with-redefs [models/find-user-by-email (fn [_ _])
                  models/register-user (fn [_ _] "some-token")
                  email/send-confirmation-email (fn [_ _ _])]
      (let [resp (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (contains (:flash resp) "You've just been an email that contains a confirmation link.")))))

  (testing "valid email format and confirmed user is in database"
    (with-redefs [models/find-user-by-email (fn [_ _] {:is_confirmed true})]
      (let [resp (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (= "That email has already been used. Please login." (:flash resp))))))

  (testing "valid email format and unconfirmed user is in database"
    (with-redefs [models/find-user-by-email (fn [_ _] {:is_confirmed false})]
      (let [resp (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
        (is (= 200 (:status resp)))
        (is (contains (:body resp) "Re-send Confirmation Email")))))

  (testing "invalid email format"
    (with-redefs [models/find-user-by-email (fn [_ _])]
      (let [resp (handlers/register-user {:params {:email "foo@example.com"}})]
        (is (= 400 (:status resp)))
        (is (contains (:body resp) "Register New User"))
        (is (contains (:body resp) "email must be from the cyrusinnovation.com domain"))))))

(deftest show-confirmation-page
  (testing "with valid confirmation token"
    (with-redefs [models/find-user-by-confirmation-token (fn [_ _] {})]
      (let [resp (handlers/show-confirmation-page {})]
        (is (= 200 (:status resp)))
        (is (contains (:body resp) "Finish Registration")))))

  (testing "with invalid confirmation token"
    (with-redefs [models/find-user-by-confirmation-token (fn [_ _])]
      (let [resp (handlers/show-confirmation-page {})]
        (is (= 302 (:status resp)))
        (is (= {"Location" "/"} (:headers resp)))
        (is (= "Invalid confirmation token." (:flash resp)))))))

(deftest update-confirmation-token
  (testing "successfully updated token"
    (with-redefs [models/update-confirmation-token (fn [_ _] "some-token")
                  email/send-confirmation-email (fn [_ _ _])]
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
  (let [valid-params {:params {:password              "secret"
                               :password_confirmation "secret"
                               :location              "Somewhere"
                               :latitude              "42.123"
                               :longitude             "-42.123"}}]
    (testing "successfully confirm user"
      (with-redefs [models/confirm-user (fn [_ _])]
        (let [resp (handlers/confirm-user valid-params)]
          (is (= 302 (:status resp)))
          (is (= {"Location" "/lunches/upcoming"} (:headers resp)))
          (is (= "Thanks for confirming your email. You are now fully registered." (:flash resp))))))

    (testing "latitude is not number"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :latitude] "not a number"))]
        (is (= 400 (:status resp)))))

    (testing "longitude is not a number"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :longitude] "not a number"))]
        (is (= 400 (:status resp)))))

    (testing "any other validation error"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :location] nil))]
        (is (= 400 (:status resp)))
        (is (contains (:body resp) "location must be present"))))))

(deftest authenticate
  (testing "successful authentication"
    (with-redefs [models/authenticate-user (fn [_ _ _] {:id "abc-123"})]
      (let [resp (handlers/authenticate {:username "jdoe" :password "secret"})]
        (is (= {:roles #{:lunchlotto.auth.handlers/user} :username "jdoe" :id "abc-123"} resp)))))
  (testing "invalid credentials"
    (with-redefs [models/authenticate-user (fn [_ _ _])]
      (let [resp (handlers/authenticate {})]
        (is (nil? resp))))))
