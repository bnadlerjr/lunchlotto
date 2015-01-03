(ns lunchlotto.common.utils-test
  (:require [clojure.test :refer :all]
            [lunchlotto.common.utils :as u]))

(deftest parse-number
  (testing "integer"
    (is (= 42 (u/parse-number "42"))))
  (testing "float"
    (is (= 3.14 (u/parse-number "3.14"))))
  (testing "negative float"
    (is (= -1.2345 (u/parse-number "-1.2345"))))
  (testing "starting w/ letters"
    (is (nil? (u/parse-number "a123"))))
  (testing "ending w/ letters"
    (is (nil? (u/parse-number "123c"))))
  (testing "code injection"
    (is (nil? (u/parse-number "(bad-function)"))))
  (testing "nil value"
    (is (nil? (u/parse-number nil))))
  (testing "empty string"
    (is (nil? (u/parse-number "")))))
