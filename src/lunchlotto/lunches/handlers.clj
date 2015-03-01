(ns lunchlotto.lunches.handlers
  (:require [clj-time.core :as time])
  (:require [lunchlotto.common.responses :as response]))

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

(def recommendation {:who-name "jdoe@cyrusinnovation.com"
                     :who-id "deadbeef-beef-4666-beef-deaddeaddead"
                     :venues [{:id "yelp-san-francisco"
                               :display_phone "+1-415-908-3801"
                               :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                               :display_address ["140 New Montgomery St"
                                                 "(b/t Natoma St & Minna St)"
                                                 "SOMA"
                                                 "San Francisco, CA 94105"]
                               :name "Yelp"
                               :rating 3
                               :rating_img_url_small "http://media1.ak.yelpcdn.com/static/201012162337205794/img/ico/stars/stars_small_3.png"
                               :review_count 3347
                               :url "http://www.yelp.com/biz/yelp-san-francisco"
                               :snippet_text "Sometimes we ask questions without reading an email thoroughly as many of us did for the last event.  In honor of Yelp, the many questions they kindly..."}
                              {:id "yelp-san-francisco"
                               :display_phone "+1-415-908-3801"
                               :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                               :display_address ["140 New Montgomery St"
                                                 "(b/t Natoma St & Minna St)"
                                                 "SOMA"
                                                 "San Francisco, CA 94105"]
                               :name "Yelp"
                               :rating 3
                               :rating_img_url_small "http://media1.ak.yelpcdn.com/static/201012162337205794/img/ico/stars/stars_small_3.png"
                               :review_count 3347
                               :url "http://www.yelp.com/biz/yelp-san-francisco"
                               :snippet_text "Sometimes we ask questions without reading an email thoroughly as many of us did for the last event.  In honor of Yelp, the many questions they kindly..."}
                              {:id "yelp-san-francisco"
                               :display_phone "+1-415-908-3801"
                               :image_url "http://s3-media2.ak.yelpcdn.com/bphoto/7DIHu8a0AHhw-BffrDIxPA/ms.jpg"
                               :display_address ["140 New Montgomery St"
                                                 "(b/t Natoma St & Minna St)"
                                                 "SOMA"
                                                 "San Francisco, CA 94105"]
                               :name "Yelp"
                               :rating 3
                               :rating_img_url_small "http://media1.ak.yelpcdn.com/static/201012162337205794/img/ico/stars/stars_small_3.png"
                               :review_count 3347
                               :url "http://www.yelp.com/biz/yelp-san-francisco"
                               :snippet_text "Sometimes we ask questions without reading an email thoroughly as many of us did for the last event.  In honor of Yelp, the many questions they kindly..."}]})

(defn index
  [req]
  (response/render :ok [:lunches :index] {:lunches upcoming
                                          :recommendation recommendation}))
