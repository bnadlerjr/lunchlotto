(ns lunchlotto.lunches.handlers-test
  (:require [clojure.test :refer :all])
  (:require [lunchlotto.lunches.handlers :as handlers]
            [lunchlotto.lunches.validations :as val]))

(deftest create-test
  (testing "successfully create lunch"
    (with-redefs [val/validate-lunch (fn [_] [true {}])]
      (let [{:keys [:status :headers :flash]} (handlers/create {})]
        (is (= 302 status))
        (is (= "/lunches" (get headers "Location")))
        (is (= "Lunch request sent." flash)))))

  (testing "cannot create lunch if invalid params"
    (with-redefs [val/validate-lunch (fn [_] [false {}])]
      (let [response (handlers/create {})]
        (is (= 400 (.status response)))
        (is (= "lunches/index.html" (.template response)))
        (is (= {} (:recommendation (.params response))))))))
