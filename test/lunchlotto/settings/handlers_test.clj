(ns lunchlotto.settings.handlers-test
  (:require [clojure.test :refer :all]
            [cemerick.friend :as friend])
  (:require [lunchlotto.settings.handlers :as handlers]
            [lunchlotto.settings.models :as models]))

(deftest settings-page
  (testing "successfully render settings page"
    (with-redefs [models/find-user-by-id (fn [_ _])]
      (let [{:keys [:status :body]} (handlers/show-settings {})]
        (is (= 200 status))
        (is (.contains body "Settings"))))))
