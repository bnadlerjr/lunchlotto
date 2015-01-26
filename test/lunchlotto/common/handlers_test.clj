(ns lunchlotto.common.handlers-test
  (:require [clojure.test :refer :all]
            [lunchlotto.routes :as routes]
            [ring.mock.request :as mock]))

(deftest common-routes-test
  (testing "not found"
    (let [resp (routes/application-routes (mock/request :get "/no-such-route"))]
      (is (= 404 (:status resp)))
      (is (.contains "Page not found." (:body resp))))))
