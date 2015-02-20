(ns lunchlotto.settings.handlers-test
  (:require [clojure.test :refer :all]
            [cemerick.friend :as friend])
  (:require [lunchlotto.settings.handlers :as handlers]
            [lunchlotto.settings.models :as models]))

(deftest show-settings
  (testing "successfully render settings page"
    (with-redefs [models/find-user-by-id (fn [_ _])]
      (let [{:keys [:status :body]} (handlers/show-settings {})]
        (is (= 200 status))
        (is (.contains body "Settings"))))))

(deftest delete-user
  (testing "successfully delete account"
    (with-redefs [models/delete-user (fn [_ _])]
      (let [{:keys [:status :headers :flash]} (handlers/delete-user {})]
        (is (= 302 status))
        (is (= {"Location" "/"} headers))
        (is (= "Your account has been deleted." flash))))))
