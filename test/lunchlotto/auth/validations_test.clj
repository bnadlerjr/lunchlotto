(ns lunchlotto.auth.validations-test
  (:require [clojure.test :refer :all]
            [lunchlotto.auth.validations :as val]
            [lunchlotto.auth.models :as models]))

(deftest validate-email
  (testing "valid if domain is correct"
    (with-redefs [models/find-user-by-email (fn [_ _] nil)]
      (let [[valid? data] (val/validate-email {:email "foo@cyrusinnovation.com"})]
        (is (true? valid?))
        (is (= {:email "foo@cyrusinnovation.com"} data)))))

  (testing "invalid if nil"
    (let [[valid? data] (val/validate-email {:email nil})]
      (is (false? valid?))
      (is (= {:email ["email must be a valid email address"]} (:errors data)))))

  (testing "invalid if empty string"
    (let [[valid? data] (val/validate-email {:email ""})]
      (is (false? valid?))
      (is (= {:email ["email must be a valid email address"]} (:errors data)))))

  (testing "invalid if domain is incorrect"
    (let [[valid? data] (val/validate-email {:email "foo@example.com"})]
      (is (false? valid?))
      (is (= {:email ["email must be from the cyrusinnovation.com domain"]} (:errors data))))))

(deftest validate-registration
  (let [params {:password              "secret"
                :password_confirmation "secret"
                :location              "somewhere"
                :latitude              42.12345
                :longitude             -42.54321}]
    (testing "valid params"
      (let [[valid? data] (val/validate-registration params)]
        (is (true? valid?))
        (is (= params data))))

    (testing "password is required"
      (let [[valid? data] (val/validate-registration
                            (assoc params :password ""
                                          :password_confirmation ""))]
        (is (false? valid?))
        (is (= {:password ["password must be present"]} (:errors data)))))

    (testing "password and confirmation must match"
      (let [[valid? data] (val/validate-registration (assoc params :password_confirmation ""))]
        (is (false? valid?))
        (is (= {:password_confirmation ["password confirmation does not match password"]} (:errors data)))))

    (testing "location is required"
      (let [[valid? data] (val/validate-registration (assoc params :location ""))]
        (is (false? valid?))
        (is (= {:location ["location must be present"]} (:errors data)))))

    (testing "latitude is required"
      (let [[valid? data] (val/validate-registration (assoc params :latitude ""))]
        (is (false? valid?))
        (is (= {:latitude ["latitude must be present"]} (:errors data)))))

    (testing "latitude must be a number"
      (let [[valid? data] (val/validate-registration (assoc params :latitude "not a number"))]
        (is (false? valid?))
        (is (= {:latitude ["latitude must be a number"]} (:errors data)))))

    (testing "longitude is required"
      (let [[valid? data] (val/validate-registration (assoc params :longitude ""))]
        (is (false? valid?))
        (is (= {:longitude ["longitude must be present"]} (:errors data)))))

    (testing "longitude must be a number"
      (let [[valid? data] (val/validate-registration (assoc params :longitude "not a number"))]
        (is (false? valid?))
        (is (= {:longitude ["longitude must be a number"]} (:errors data)))))))
