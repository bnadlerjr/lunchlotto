(ns lunchlotto.common.middleware-test
  (:require [clojure.test :refer :all]
            [lunchlotto.common.middleware :refer [wrap-coerce-params
                                                  wrap-simulated-methods]]))

(deftest wrap-simulated-methods-middleware
  (let [middleware (wrap-simulated-methods identity)]
    (testing "POST request with a DELETE param"
      (let [request {:request-method :post
                     :params {"_method" "DELETE"}}
            response (middleware request)]
        (is (= :delete (:request-method response)))))

    (testing "POST request with a PUT param"
      (let [request {:request-method :post
                     :params {"_method" "PUT"}}
            response (middleware request)]
        (is (= :put (:request-method response)))))

    (testing "POST request without a _method"
      (let [request {:request-method :post}
            response (middleware request)]
        (is (= :post (:request-method response)))))

    (testing "POST request with an unknown param"
      (let [request {:request-method :post
                     :params {"_method" "foo"}}
            response (middleware request)]
        (is (= :post (:request-method response)))))))

(deftest wrap-coerce-params-middleware
  (let [middleware (wrap-coerce-params identity)
        request {:params {:foo "1" :bar "text"}}
        response (middleware request)]
    (is (= {:params {:foo 1 :bar "text"}} response))))
