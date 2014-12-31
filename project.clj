(defproject lunchlotto "0.1.0-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [compojure "1.3.1"]
                 [http-kit "2.1.19"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [java-jdbc/dsl "0.1.1"]
                 [postgresql/postgresql "9.1-901-1.jdbc4"]
                 [hiccup "1.0.5"]
                 [environ "1.0.0"]]
  :min-lein-version "2.0.0"
  :uberjar-name "lunchlotto.jar"
  :main lunchlotto.app
  :profiles {:dev {:env {:database-url "jdbc:postgresql://localhost/webdev"
                         :debug true}
                   :dependencies [[ring/ring-mock "0.2.0"]
                                  [prone "0.8.0"]]}
             :test {:env {:database-url "jdbc:postgresql://localhost/webdev_test"}}}
  :plugins [[lein-environ "1.0.0"]
            [lein-ancient "0.5.5"]
            [lein-kibit "0.0.8"]
            [jonase/eastwood "0.2.1"]
            [lein-bikeshed "0.2.0"]])
