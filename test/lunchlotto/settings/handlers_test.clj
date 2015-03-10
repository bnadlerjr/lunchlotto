(ns lunchlotto.settings.handlers-test
  (:require [clojure.test :refer :all]
            [cemerick.friend :as friend])
  (:require [lunchlotto.settings.handlers :as handlers]
            [lunchlotto.settings.models :as models]
            [lunchlotto.settings.validations :as val]
            [lunchlotto.auth.utils :as auth-utils]))

(deftest show-settings
  (testing "successfully render settings page"
    (with-redefs [models/find-user-by-id (fn [_ _] {:id 1})]
      (let [response (handlers/show-settings {})]
        (is (= 200 (.status response)))
        (is (= "settings/show.html" (.template response)))
        (is (= {:user {:id 1}} (.params response)))))))

(deftest update-settings
  (with-redefs [models/find-user-by-id (fn [_ _] true)]
    (testing "successfully update settings"
      (with-redefs [models/update-settings (fn [_ _] true)
                    val/validate-settings (fn [_ _] [true {}])]
        (let [{:keys [:status :headers :flash]} (handlers/update-settings {})]
          (is (= 302 status))
          (is (= "/settings" (get headers "Location")))
          (is (= "Your settings have been updated." flash)))))

    (testing "cannot update because of validation error"
      (with-redefs [models/update-settings (fn [_ _] true)
                    val/validate-settings (fn [_ _] [false {}])]
        (let [response (handlers/update-settings {})]
          (is (= 400 (.status response)))
          (is (= "settings/show.html" (.template response)))
          (is (= {:user {:email nil}} (.params response))))))))

(deftest delete-user
  (testing "successfully delete account"
    (with-redefs [models/delete-user (fn [_ _])]
      (let [{:keys [:status :headers :flash]} (handlers/delete-user {})]
        (is (= 302 status))
        (is (= "/" (get headers "Location")))
        (is (= "Your account has been deleted." flash))))))
