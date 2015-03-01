(ns lunchlotto.settings.validations-test
  (:require [clojure.test :refer :all]
            [lunchlotto.settings.validations :as val]))

(deftest validate-settings-test
  (let [params {:current_password "secret"
                :location "Somewhere"
                :latitude 42.123
                :longitude -42.321}
        encrypted-password "$2a$11$EUcV6lgD0/7zUEe82r0DcO7SuN8/JTASdtPX1AaoVvfCRAFahIA0O"] ; secret

  (testing "valid params"
    (let [[valid? data] (val/validate-settings params encrypted-password)]
      (is (true? valid?))
      (is (= params data))))

  (testing "current password is required"
    (let [[valid? data] (val/validate-settings
                          (assoc params :current_password "")
                          encrypted-password)]
      (is (false? valid?))
      (is (= {:current_password ["current_password must be present"]} (:errors data)))))


  (testing "current password must be correct"
    (let [[valid? data] (val/validate-settings
                          (assoc params :current_password "not-my-password")
                          encrypted-password)]
      (is (false? valid?))
      (is (= {:current_password ["current password is invalid"]} (:errors data)))))

  (testing "new password must be confirmed"
    (let [[valid? data] (val/validate-settings
                           (assoc params
                                  :new_password "foo"
                                  :new_password_confirmation "bar")
                           encrypted-password)]
      (is (false? valid?))
      (is (= {:new_password_confirmation ["password confirmation does not match password"]} (:errors data)))))

  (testing "location is required"
    (let [[valid? data] (val/validate-settings
                          (assoc params :location "")
                          encrypted-password)]
      (is (false? valid?))
      (is (= {:location ["location must be present"]} (:errors data)))))

  (testing "latitude is required"
    (let [[valid? data] (val/validate-settings
                           (assoc params :latitude "")
                           encrypted-password)]
      (is (false? valid?))
      (is (= {:latitude ["latitude must be present"]} (:errors data)))))

  (testing "latitude is a number"
    (let [[valid? data] (val/validate-settings
                           (assoc params :latitude "not a number")
                           encrypted-password)]
      (is (false? valid?))
      (is (= {:latitude ["latitude must be a number"]} (:errors data)))))

  (testing "longitude is required"
    (let [[valid? data] (val/validate-settings
                           (assoc params :longitude "")
                           encrypted-password)]
      (is (false? valid?))
      (is (= {:longitude ["longitude must be present"]} (:errors data)))))

  (testing "longitude is a number"
    (let [[valid? data] (val/validate-settings
                           (assoc params :longitude "not a number")
                           encrypted-password)]
      (is (false? valid?))
      (is (= {:longitude ["longitude must be a number"]} (:errors data)))))))
