(ns asuki.back.models.util
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [asuki.back.config :refer [db-spec]]))

(defn get-list-with-total [query-get-records]
  (jdbc/with-db-transaction [db-transaction db-spec]
    {:list (jdbc/query db-transaction query-get-records)
     :total (-> (jdbc/query db-transaction "SELECT FOUND_ROWS()") first vals first)}))

(defn parse-json [input]
  (try
    (let [result (json/read-str input)] {:parsed result})
    (catch Exception e {:error (str (.getMessage e))})))
