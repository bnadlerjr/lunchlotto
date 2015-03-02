(ns lunchlotto.services.venues
  (:require [environ.core :refer [env]]
            [gws.yelp.client :as client]
            [gws.yelp.api :as api]))

(def yelp-client (client/create (env :yelp-consumer-key)
                                (env :yelp-consumer-secret)
                                (env :yelp-token)
                                (env :yelp-token-secret)))

(defn find-venues
  "Query Yelp for restaurant venues centered around given latitude and
  longitude."
  [latitude longitude]
  (:businesses
    (api/search yelp-client
                {:category_filter "restaurants"
                 :ll              (str latitude "," longitude)
                 :radius_filter   400})))
