(ns lunchlotto.lunches.validations-test
  (:require [clojure.test :refer :all]
            [lunchlotto.lunches.validations :as val]))

(deftest validate-lunch-test
  (let [params {:who   "fbfc5386-9f95-40c6-826d-3b5d9cf8cf52"
                :when  "2014-03-10T12:00"
                :venue "ck14-the-crooked-knife-new-york"}]

  (testing "valid params"
    (let [[valid? data] (val/validate-lunch params)]
      (is (true? valid?))
      (is (= params data))))

  (testing "who is required"
    (let [[valid? data] (val/validate-lunch (assoc params :who ""))]
      (is (false? valid?))
      (is (= {:who ["who must be present"]} (:errors data)))))

  (testing "when is required"
    (let [[valid? data] (val/validate-lunch (assoc params :when ""))]
      (is (false? valid?))
      (is (= {:when ["when must be present"]} (:errors data)))))

  (testing "when must be a datetime"
    (let [[valid? data] (val/validate-lunch (assoc params :when "not a date"))]
      (is (false? valid?))
      (is (= {:when ["when must be a valid date"]} (:errors data)))))

  (testing "venue is required"
    (let [[valid? data] (val/validate-lunch (assoc params :venue ""))]
      (is (false? valid?))
      (is (= {:venue ["venue must be present"]} (:errors data)))))))
