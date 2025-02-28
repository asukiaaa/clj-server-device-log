(ns back.models.util
  (:require [clj-time.core :as cljt]
            [clj-time.format :as cljt-format]
            [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [clojure.core :refer [format]]
            [clojure.string :refer [escape join]]
            [back.config :refer [db-spec]]
            [back.util.encryption :as encryption]))

(def time-format-yyyymmdd-hhmmss (cljt-format/formatter "YYYY-MM-dd HH:mm:ss"))
(def ^:private str-alphabets-and-number "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
(def ^:private str-parentesis-and-special-chars ":[]\\/.,\"!#$%&'()-^")
(def key-authorization-bearer :authorization_bearer)

(defn build-random-str-complex [len]
  (apply str (repeatedly len #(rand-nth (str str-alphabets-and-number str-parentesis-and-special-chars)))))

(defn build-random-str-alphabets-and-number [len]
  (apply str (repeatedly len #(rand-nth str-alphabets-and-number))))

(defn get-list-with-total [query-get-records & [{:keys [build-item transaction]}]]
  #_(println query-get-records)
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (let [items (jdbc/query transaction query-get-records)
          total (-> (jdbc/query transaction "SELECT FOUND_ROWS()") first vals first)
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

(defn build-input-str-for-str [val]
  (let [val (escape-for-sql val)]
    (if (nil? val) "null" (format "\"%s\"" val))))

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

(defn get-by-id [id name-table & [{:keys [transaction str-keys-select str-key-id str-before-where build-item]}]]
  (let [str-keys-select (or str-keys-select "*")
        str-key-id (or str-key-id (format "%s.id" name-table))
        query (->> [(format "SELECT %s FROM %s" str-keys-select name-table)
                    str-before-where
                    (format "WHERE %s = ?"  str-key-id)]
                   (remove nil?)
                   (join " "))
        item (first (jdbc/query (or transaction db-spec) [query id]))]
    (if build-item (build-item item) item)))

(defn create [key-table params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [t-con (or transaction db-spec)]
    (jdbc/insert! t-con key-table params)
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id #_"(SELECT LAST_INSERT_ID())" id (name key-table) {:transaction t-con})]
      item)))

(defn get-list-with-total-with-building-query [name-table params & [{:keys [str-where str-keys-select transaction str-before-where str-order build-item]}]]
  (-> (build-query-get-index name-table {:str-keys-select str-keys-select})
      (#(if-not (empty? str-before-where) (str % " " str-before-where) %))
      (#(if-not (empty? str-where) (str % " WHERE " str-where) %))
      (#(if-not (empty? str-order) (str % " ORDER BY " str-order) %))
      (append-limit-offset-by-limit-page-params params)
      (get-list-with-total {:transaction transaction :build-item build-item})))

(defn build-authorization-bearer [key-str key-table key-key-str]
  (when key-str
    (encryption/encode
     {key-table {key-key-str key-str}
      :created_at (cljt-format/unparse time-format-yyyymmdd-hhmmss (cljt/now))})))
