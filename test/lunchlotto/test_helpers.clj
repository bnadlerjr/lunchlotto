(ns lunchlotto.test-helpers
  (:require [clojure.test :refer [deftest is]]))

(def ^:dynamic mock-calls)

(defn stub-fn
  [the-function return-value]
  (swap! mock-calls assoc the-function [])
  (fn [& args]
    (swap! mock-calls update-in [the-function] conj args)
    return-value))

(defn mock-fn
  [the-function]
  (stub-fn the-function nil))

(defmacro verify-call-times-for
  [fn-name number]
  `(is (= ~number (count (@mock-calls ~(keyword fn-name))))))

(defmacro verify-nth-call-args-for
  [n fn-name & args]
  `(is (= '~args (nth (@mock-calls ~(keyword fn-name)) (dec ~n)))))

(defmacro verify-first-call-args-for
  [fn-name & args]
  `(verify-nth-call-args-for 1 ~fn-name ~@args))

(defmacro mocking
  [fn-names & body]
  (let [mocks (map #(list 'mock-fn (keyword %)) fn-names)]
    `(with-redefs [~@(interleave fn-names mocks)]
       ~@body)))

(defmacro stubbing
  [stub-forms & body]
  (let [stub-pairs (partition 2 stub-forms)
        real-fns (map first stub-pairs)
        returns (map last stub-pairs)
        stub-fns (map #(list 'stub-fn %1 %2) real-fns returns)]
    `(with-redefs [~@(interleave real-fns stub-fns)]
       ~@body)))

(defmacro defmocktest
  [test-name & body]
  `(deftest ~test-name
     (binding [mock-calls (atom {})]
       (do ~@body))))
