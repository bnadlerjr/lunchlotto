(ns lunchlotto.common.tags
  (:require [ring.util.anti-forgery :as ring]
            [selmer.parser :as selmer]))

(selmer/add-tag!
  :csrf-tag
  (fn [_ _] (ring/anti-forgery-field)))
