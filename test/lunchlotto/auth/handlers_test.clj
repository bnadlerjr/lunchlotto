(ns lunchlotto.auth.handlers-test
  (:require [clojure.test :refer :all]
            [lunchlotto.test-helpers :refer :all]
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
      (is (= 200 (.status resp)))
      (is (= "auth/register.html" (.template resp))))))

(deftest show-login-page
  (testing "successfully render login page"
    (let [resp (handlers/show-login-page {})]
      (is (= 200 (.status resp)))
      (is (= "auth/login.html" (.template resp))))))

(deftest failed-login
  (testing "successfully render failed login page"
    (let [resp (handlers/failed-login {})]
      (is (= 302 (:status resp)))
      (is (= "/login" (get-in resp [:headers "Location"])))
      (is (= "Sorry, that email and password combination is incorrect."
             (:flash resp))))))

(defmocktest register-user-test
  (testing "valid email format and email is not in database"
    (stubbing [models/find-user-by-email nil
               models/register-user "some-token"]
      (mocking [email/send-confirmation-email]
        (let [{:keys [:status :headers :flash]} (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
          (is (= 302 status))
          (is (= "/" (get headers "Location")))
          (is (contains flash "You've just been sent an email"))
          (verify-call-times-for email/send-confirmation-email 1)
          (verify-first-call-args-for email/send-confirmation-email "jdoe@cyrusinnovation.com" "some-token" {:params {:email "jdoe@cyrusinnovation.com"}})))))

  (testing "valid email format and confirmed user is in database"
    (stubbing [models/find-user-by-email {:is_confirmed true}]
      (mocking [email/send-confirmation-email]
        (let [{:keys [:status :headers :flash]} (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
          (is (= 302 status))
          (is (= "/" (get headers "Location")))
          (is (contains flash "That email has already been used."))
          (verify-call-times-for email/send-confirmation-email 0)))))

  (testing "valid email format and unconfirmed user is in database"
    (stubbing [models/find-user-by-email {:is_confirmed false}]
      (mocking [email/send-confirmation-email]
        (let [response (handlers/register-user {:params {:email "jdoe@cyrusinnovation.com"}})]
          (is (= 200 (.status response)))
          (is (= "auth/register.html" (.template response)))
          (is (= {:user {:errors {:email ["email has already been used."]
                                  :can_resend_token? true}
                         :email  "jdoe@cyrusinnovation.com"}}
                 (.params response)))
          (verify-call-times-for email/send-confirmation-email 0)))))

  (testing "invalid email format"
    (stubbing [models/find-user-by-email nil]
      (mocking [email/send-confirmation-email]
        (let [response (handlers/register-user {:params {:email "foo@example.com"}})]
          (is (= 400 (.status response)))
          (is (= "auth/register.html" (.template response)))
          (is (= {:user {:errors {:email ["email must be from the cyrusinnovation.com domain"]}
                         :email  "foo@example.com"}}
                 (.params response)))
          (verify-call-times-for email/send-confirmation-email 0))))))

(defmocktest show-confirmation-page-test
  (testing "with valid confirmation token"
    (stubbing [models/find-user-by-confirmation-token {:email "jdoe@cyrusinnovation.com"}]
      (let [response (handlers/show-confirmation-page {})]
        (is (= 200 (.status response)))
        (is (= "auth/confirm.html" (.template response)))
        (is (= {:user {:email "jdoe@cyrusinnovation.com"}} (.params response))))))

  (testing "with invalid confirmation token"
    (stubbing [models/find-user-by-confirmation-token nil]
      (let [{:keys [:status :headers :flash]} (handlers/show-confirmation-page {})]
        (is (= 302 status))
        (is (= "/" (get headers "Location")))
        (is (= "Invalid confirmation token." flash))))))

(deftest update-confirmation-token
  (testing "successfully updated token"
    (with-redefs [models/update-confirmation-token (fn [_ _] "some-token")
                  email/send-confirmation-email (fn [_ _ _])]
      (let [resp (handlers/update-confirmation-token {:params {:email "jdoe@cyrusinnovation.com"}})]
        (is (= 302 (:status resp)))
        (is (= "/" (get-in resp [:headers "Location"])))
        (is (contains (:flash resp) "You've just been sent an email that contains a confirmation link.")))))

  (testing "invalid email format"
    (let [resp (handlers/update-confirmation-token {:params {:email "foo@example.com"}})]
      (is (= 400 (.status resp)))
      (is (= {:user {:errors {:can_resend_token? true
                              :email ["email must be from the cyrusinnovation.com domain"]}
                     :email  "foo@example.com"}}
             (.params resp))))))

(deftest confirm-user
  (let [valid-params {:params {:password              "secret"
                               :password_confirmation "secret"
                               :location              "Somewhere"
                               :latitude              42.123
                               :longitude             -42.123}}]
    (testing "successfully confirm user"
      (with-redefs [models/confirm-user (fn [_ _])
                    models/find-user-by-email (fn [_])]
        (let [resp (handlers/confirm-user valid-params)]
          (is (= 302 (:status resp)))
          (is (= "/lunches/upcoming" (get-in resp [:headers "Location"])))
          (is (= "Thanks for confirming your email. You are now fully registered." (:flash resp))))))

    (testing "latitude is not number"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :latitude] "not a number"))]
        (is (= 400 (.status resp)))
        (is (= "auth/confirm.html" (.template resp)))
        (is (= ["latitude must be a number"] (get-in (.params resp) [:user :errors :latitude])))))

    (testing "longitude is not a number"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :longitude] "not a number"))]
        (is (= 400 (.status resp)))
        (is (= "auth/confirm.html" (.template resp)))
        (is (= ["longitude must be a number"] (get-in (.params resp) [:user :errors :longitude])))))

    (testing "any other validation error"
      (let [resp (handlers/confirm-user (assoc-in valid-params [:params :location] nil))]
        (is (= 400 (.status resp)))
        (is (= "auth/confirm.html" (.template resp)))
        (is (= ["location must be present"] (get-in (.params resp) [:user :errors :location])))))))

(deftest authenticate
  (testing "successful authentication"
    (with-redefs [models/authenticate-user (fn [_ _ _] {:id "abc-123"})]
      (let [resp (handlers/authenticate {:username "jdoe" :password "secret"})]
        (is (= {:roles #{:lunchlotto.auth.handlers/user} :username "jdoe" :id "abc-123"} resp)))))
  (testing "invalid credentials"
    (with-redefs [models/authenticate-user (fn [_ _ _])]
      (let [resp (handlers/authenticate {})]
        (is (nil? resp))))))
