(defproject lunchlotto "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[bouncer "0.3.2" :exclusions [org.clojure/tools.reader]]
                 [clj-time "0.9.0"]
                 [compojure "1.3.3"]
                 [crypto-password "0.1.3"]
                 [crypto-random "1.2.0"]
                 [environ "1.0.0"]
                 [postmark "1.1.0" :exclusions [org.clojure/clojure
                                                commons-logging
                                                slingshot
                                                commons-codec
                                                org.apache.httpcomponents/httpclient
                                                org.clojure/tools.reader]]
                 [com.cemerick/friend "0.2.1"]
                 [gws/clj-yelp "0.3.1"]
                 [http-kit "2.1.19"]
                 [java-jdbc/dsl "0.1.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [pandect "0.5.1"]
                 [postgresql/postgresql "9.3-1102.jdbc41"]
                 [prone "0.8.1"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [selmer "0.8.2"]
                 [yesql  "0.5.0-rc2"]]
  :min-lein-version "2.0.0"
  :uberjar-name "lunchlotto.jar"
  :main lunchlotto.app
  :profiles {:dev {}
             :test {:env {:database-url "jdbc:postgresql://localhost/lunchlotto_test"}
                    :dependencies [[ring/ring-mock "0.2.0"]]}}
  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [lein-kibit "0.0.8"]
            [jonase/eastwood "0.2.1"]
            [lein-bikeshed "0.2.0"]
            [lein-cloverage "1.0.2"]]
  :aliases {"lint" ["do" ["ancient"] ["kibit"] ["eastwood"]]})
