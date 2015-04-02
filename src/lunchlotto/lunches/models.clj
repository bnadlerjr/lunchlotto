(ns lunchlotto.lunches.models
  (:require [clojure.java.jdbc :as jdbc])
  (:require [lunchlotto.common.utils :as utils]
            [lunchlotto.protocols :as protocols]))

(defn get-pairing
  [db user-id]
  (let [all-users      (jdbc/query db ["SELECT * FROM users"])
        [user buddies] (utils/separate #(= user-id (:id %)) all-users)]
    [(first user) (rand-nth buddies)]))

(defn calculate-venue-coords
  "Calculate latitude and longitude coordinates to center venue search based
  on two users."
  [user buddy]
  (let [latitude  (/ (+ (:latitude user) (:latitude buddy)) 2.0)
        longitude (/ (+ (:longitude user) (:longitude buddy)) 2.0)]
    [latitude longitude]))

(defn make-recommendation
  "Make a lunch recommendation."
  [db venues user-id]
  (let [[user buddy] (get-pairing db user-id)
        [latitude longitude] (calculate-venue-coords user buddy)]
    {:who-name (:email buddy)
     :who-id   (:id buddy)
     :venues   (take 3 (shuffle (protocols/find-by-location venues latitude longitude)))}))
