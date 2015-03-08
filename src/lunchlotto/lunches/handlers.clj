(ns lunchlotto.lunches.handlers
  (:require [clj-time.core :as time]
            [environ.core :refer [env]]
            [cemerick.friend :as friend])
  (:require [lunchlotto.common.responses :as response]
            [lunchlotto.lunches.models :as models]
            [lunchlotto.lunches.validations :as val]
            [lunchlotto.services.venues :as venues]))

(def db (env :database-url))

(def upcoming [{:status "Confirmed"
                :attendees ["jdoe@cyrusinnovation.com"]
                :lunch_at (time/now)
                :venue {:name "Yelp"
                        :display_phone "+1-415-908-3801"
                        :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                        :display_address ["140 New Montgomery St"
                                          "(b/t Natoma St & Minna St)"
                                          "SOMA"
                                          "San Francisco, CA 94105"]
                        :url "http://www.yelp.com/biz/yelp-san-francisco"}}
               {:status "Pending"
                :attendees ["jdoe@cyrusinnovation.com"]
                :lunch_at (time/now)
                :venue {:name "Yelp"
                        :display_phone "+1-415-908-3801"
                        :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                        :display_address ["140 New Montgomery St"
                                          "(b/t Natoma St & Minna St)"
                                          "SOMA"
                                          "San Francisco, CA 94105"]
                        :url "http://www.yelp.com/biz/yelp-san-francisco"}}
               {:status "Confirmed"
                :attendees ["jdoe@cyrusinnovation.com"]
                :lunch_at (time/now)
                :venue {:name "Yelp"
                        :display_phone "+1-415-908-3801"
                        :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                        :display_address ["140 New Montgomery St"
                                          "(b/t Natoma St & Minna St)"
                                          "SOMA"
                                          "San Francisco, CA 94105"]
                        :url "http://www.yelp.com/biz/yelp-san-francisco"}}])

(defn index
  [req]
  (response/render
    :ok
    "lunches/index"
    {:lunches upcoming
     :recommendation (models/make-recommendation
                       db (:id (friend/current-authentication req)))}))

(defn create
  [req]
  (let [[valid? data] (val/validate-lunch (:params req))]
    (if valid?
      (response/redirect "/lunches" "Lunch request sent.")
      (response/render :bad-request "lunches/index"
                       {:lunches upcoming
                        :recommendation data}))))
