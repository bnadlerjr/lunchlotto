(ns lunchlotto.common.utils-test
  (:require [clojure.test :refer :all]
            [lunchlotto.common.utils :as u]))

(deftest fmap
  (is (= {:foo 2 :bar 3 :baz 4}
         (u/fmap #(+ % 1) {:foo 1 :bar 2 :baz 3}))))

(deftest string->number
  (testing "integer"
    (is (= 42 (u/string->number "42"))))
  (testing "negative integer"
    (is (= -10 (u/string->number "-10"))))
  (testing "float"
    (is (= 3.14 (u/string->number "3.14"))))
  (testing "negative float"
    (is (= -1.2345 (u/string->number "-1.2345"))))
  (testing "starting w/ letters"
    (is (= "a123" (u/string->number "a123"))))
  (testing "ending w/ letters"
    (is (= "123c" (u/string->number "123c"))))
  (testing "letters in the middle"
    (is (= "1c3" (u/string->number "1c3"))))
  (testing "nil value"
    (is (nil? (u/string->number nil))))
  (testing "empty string"
    (is (= "" (u/string->number "")))))
