(ns lunchlotto.common.handlers-test
  (:require [clojure.test :refer :all]
            [lunchlotto.common.handlers :refer [common-routes]]
            [ring.mock.request :as mock]))

(deftest common-routes-test
  (testing "not found"
    (let [resp (common-routes (mock/request :get "/no-such-route"))]
      (is (= 404 (:status resp)))
      (is (.contains "Page not found." (:body resp))))))
