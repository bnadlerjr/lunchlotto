(ns lunchlotto.settings.handlers-test
  (:require [clojure.test :refer :all]
            [cemerick.friend :as friend])
  (:require [lunchlotto.settings.handlers :as handlers]
            [lunchlotto.settings.models :as models]
            [lunchlotto.auth.utils :as auth-utils]))

(deftest show-settings
  (testing "successfully render settings page"
    (with-redefs [models/find-user-by-id (fn [_ _] {:id 1})]
      (let [response (handlers/show-settings {})]
        (is (= 200 (.status response)))
        (is (= "lunchlotto/settings/templates/show.html" (.template response)))
        (is (= {:user {:id 1}} (.params response)))))))

(deftest update-settings
  (testing "successfully update settings"
    (with-redefs [models/update-settings (fn [_ _] true)
                  models/find-user-by-id (fn [_ _] {})
                  auth-utils/check-password (fn [_ _] true)]
      (let [req {:params {:current_password "secret"
                          :location "Somewhere"
                          :latitude -42.123
                          :longitude 42.321}}
            {:keys [:status :headers :flash]} (handlers/update-settings req)]
        (is (= 302 status))
        (is (= "/settings" (get headers "Location")))
        (is (= "Your settings have been updated." flash)))))

  (testing "cannot update because of validation error"
    (let [req {:params {:current_password "secret"}}
          response (handlers/update-settings req)]
      (is (= 400 (.status response)))
      (is (= "lunchlotto/settings/templates/show.html" (.template response)))
      (is (= {:user {:email nil
                     :errors {:longitude ["longitude must be present"]
                              :latitude  ["latitude must be present"]
                              :location  ["location must be present"]}
                     :current_password  "secret"}}
             (.params response))))))

(deftest delete-user
  (testing "successfully delete account"
    (with-redefs [models/delete-user (fn [_ _])]
      (let [{:keys [:status :headers :flash]} (handlers/delete-user {})]
        (is (= 302 status))
        (is (= "/" (get headers "Location")))
        (is (= "Your account has been deleted." flash))))))
