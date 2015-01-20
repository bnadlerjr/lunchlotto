(defproject lunchlotto "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[bouncer "0.3.1"]
                 [clj-time "0.9.0"]
                 [compojure "1.3.1"]
                 [crypto-password "0.1.3"]
                 [crypto-random "1.2.0"]
                 [environ "1.0.0"]
                 [hiccup "1.0.5"]
                 [http-kit "2.1.19"]
                 [java-jdbc/dsl "0.1.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [pandect "0.5.0"]
                 [postgresql/postgresql "9.1-901-1.jdbc4"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]]
  :min-lein-version "2.0.0"
  :uberjar-name "lunchlotto.jar"
  :main lunchlotto.app
  :profiles {:dev {:env {:database-url "jdbc:postgresql://localhost/lunchlotto"
                         :debug true}
                   :dependencies [[ring/ring-mock "0.2.0"]
                                  [prone "0.8.0"]]}
             :test {:env {:database-url "jdbc:postgresql://localhost/lunchlotto_test"}}}
  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.5.5"]
            [lein-kibit "0.0.8"]
            [jonase/eastwood "0.2.1"]
            [lein-bikeshed "0.2.0"]]
  :aliases {"lint" ["do" ["ancient"] ["kibit"] ["eastwood"]]})
