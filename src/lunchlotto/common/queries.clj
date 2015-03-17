(ns lunchlotto.common.queries
  (:require [environ.core :refer [env]]
            [yesql.core :refer [defqueries]]))

(defqueries "queries.sql" {:connection (env :database-url)})
