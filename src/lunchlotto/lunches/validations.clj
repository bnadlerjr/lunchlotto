(ns lunchlotto.lunches.validations
  (:require [bouncer.core :as bouncer]
            [bouncer.validators :as validators]))

(defn- wrap-validation
  "Takes bouncer validation results and returns a vector consisting of a
  boolean indicating if the validation has errors and the same map bouncer
  provides except that bouncer.core/errors is renamed to errors."
  [[errors params]]
  [(nil? errors)
   (clojure.set/rename-keys params {:bouncer.core/errors :errors})])

(defn validate-lunch
  [params]
  (wrap-validation
    (bouncer/validate
      params
      {:who   [validators/required]
       :when  [validators/required validators/datetime]
       :venue [validators/required]})))
