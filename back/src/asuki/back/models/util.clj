(ns asuki.back.models.util
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [clojure.core :refer [format]]
            [clojure.string :refer [escape join]]
            [asuki.back.config :refer [db-spec]]))

(defn get-list-with-total [query-get-records & [{:keys [build-item]}]]
  (println :query-get-records query-get-records)
  (jdbc/with-db-transaction [db-transaction db-spec]
    (let [items (jdbc/query db-transaction query-get-records)
          total (-> (jdbc/query db-transaction "SELECT FOUND_ROWS()") first vals first)
          items (if (nil? build-item) items (map build-item items))]
      {:list items
       :total total})))

(defn parse-json [input]
  (try
    (let [result (json/read-str input)] {:parsed result})
    (catch Exception e {:error (str (.getMessage e))})))

(defn escape-for-sql [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn build-query-get-index [name-table & [{:keys [with-calc-found-rows str-keys-select]}]]
  (let [with-calc-found-rows (if (nil? with-calc-found-rows) true with-calc-found-rows)
        str-keys-select (if (nil? str-keys-select) "*" str-keys-select)]
    (->> ["SELECT" (when with-calc-found-rows "SQL_CALC_FOUND_ROWS")
          str-keys-select
          "FROM" (escape-for-sql name-table)]
         (filter seq)
         (join " "))))

(defn append-limit-offset-by-limit-page-params [str-query {:keys [limit page]}]
  (if (or limit page)
    (let [offset (* limit page)]
      (format "%s LIMIT %d OFFSET %d" str-query (int limit) (int offset)))
    str-query))

(defn get-by-id [id name-table & [{:keys [transaction]}]]
  (let [query (format "SELECT * FROM %s WHERE id = ?" name-table)]
    (first (jdbc/query (or transaction db-spec) [query id]))))
