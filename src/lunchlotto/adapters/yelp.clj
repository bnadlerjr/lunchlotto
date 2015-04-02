(ns lunchlotto.adapters.yelp
  (:require [gws.yelp.api :as api])
  (:require [lunchlotto.protocols :as protocols]))

(defrecord Yelp
  [client]
  protocols/Venue

  (find-by-location [this latitude longitude]
    (:businesses
      (api/search client
                  {:category_filter "restaurants"
                   :ll              (str latitude "," longitude)
                   :radius_filter   400}))))
