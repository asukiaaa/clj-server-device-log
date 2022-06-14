(ns asuki.back.models.raw-device-log
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join escape]]
            [clojure.core :refer [re-find re-matcher]]
            [clojure.data.json :as json]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [asuki.back.config :refer [db-spec]]))

(def defaults
  {:limit 100
   :order [{:key "created_at" :dir "DESC"}]})

(defn escape-for-sql [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn filter-key [key]
  (when (.contains ["created_at" "id" "data"] key)
    key))

(defn filter-order-dir [dir]
  (when (.contains ["DESC" "desc" "ASC" "asc"] dir)
    dir))

(defn build-target-key [{:keys [key json-key table-key]}]
  #_(println "build-target-key" key json-key table-key)
  (let [escaped-key (filter-key key)
        key-with-table (when-not (nil? escaped-key)
                         (str (if table-key (str table-key ".") "") escaped-key))]
    (if (nil? json-key)
      key-with-table
      (format "JSON_VALUE(%s,\"$.%s\")"
              key-with-table
              (if (string? json-key)
                (escape-for-sql json-key)
                (join "." (for [k json-key] (escape-for-sql k))))))))

(defn build-query-order [order]
  #_(println "build-query-order " order)
  (str "ORDER BY "
       (join ", " (for [item order]
                    (join " " [(build-target-key {:key (or (get item "key") (:key item))
                                                  :json-key (or (:json-key item) (get item "json_key"))})
                               (filter-order-dir (or (get item "dir") (:dir item)))])))))

(defn filter-target-action [action]
  (case action
    "=" "="
    "gt" ">"
    "gte" ">="
    "lt" "<"
    "lte" "<="
    "not_null" "IS NOT NULL"
    nil))

(declare build-query-item-where)

(defn build-where-not-exists [not-exists {:keys [db-table-key base-table-key]}]
  #_(println "build-where-not-exists" db-table-key base-table-key)
  (let [this-table-key (str base-table-key "1")]
    (join " " ["NOT EXISTS (SELECT 1 FROM" db-table-key "AS" this-table-key "WHERE"
               (join " AND " (for [item not-exists]
                               (let [key (build-query-item-where
                                          item
                                          {:base-table-key base-table-key
                                           :this-table-key this-table-key})]
                                 #_(print "key from build-query-item-where" key)
                                 key)))
               ")"])))

(defn build-query-item-where [args {:keys [db-table-key base-table-key this-table-key]}]
  #_(println "build query item where " args db-table-key base-table-key this-table-key)
  (let [action (get args "action")
        value (get args "value")
        not-exists (get args "not_exists")
        key (get args "key")
        json-key (get args "json_key")
        str-hours-from-action (when (string? action)
                                (-> (re-matcher #"in-hours-(\d+)$" action)
                                    re-find
                                    second))
        target-key (build-target-key {:key key
                                      :json-key json-key
                                      :table-key base-table-key})
        target-action (filter-target-action action)
        target-value (cond
                       (string? value) (str "\"" (escape-for-sql value) "\"")
                       (not (nil? this-table-key))
                       (build-target-key {:key key
                                          :json-key json-key
                                          :table-key this-table-key})
                       :else value)]
    #_(println "build-query-item-where" target-key target-action target-value base-table-key this-table-key not-exists)
    (cond
      (not (nil? not-exists))
      (build-where-not-exists not-exists {:db-table-key db-table-key :base-table-key base-table-key})
      (not (nil? str-hours-from-action))
      (join " " [target-key ">"
                 (f/unparse (f/formatter "\"YYYY-MM-dd HH:mm:ss\"")
                            (t/minus (t/now) (t/hours (Integer. str-hours-from-action))))])
      :else (join " " [target-key target-action target-value]))))

(defn build-query-where [{:keys [where db-table-key base-table-key]}]
  (when-not (or (nil? where) (empty? where))
    #_(println "where" where)
    (str "WHERE "
         (join " AND " (for [item where]
                         #_(println "item" item)
                         (build-query-item-where item {:db-table-key db-table-key
                                                       :base-table-key base-table-key}))))))

(defn get-records-with-total [& [args]]
  (println "get-records-with-total" args)
  (let [limit (or (:limit args) (:limit defaults))
        order (or (when-let [str-order (:order args)]
                    (json/read-str str-order))
                  (:order defaults))
        where (json/read-str (:where args))
        db-table-key "raw_device_log"
        base-table-key "rdl"
        str-query (join " " ["select SQL_CALC_FOUND_ROWS * from" db-table-key "as" base-table-key
                             (build-query-where {:where where
                                                 :db-table-key db-table-key
                                                 :base-table-key base-table-key})
                             (build-query-order order)
                             "limit 0," limit])]
    (println "str-query " str-query)
    (jdbc/with-db-transaction [db-transaction db-spec]
      {:records (jdbc/query db-transaction str-query)
       :total (-> (jdbc/query db-transaction "SELECT FOUND_ROWS()") first vals first)})))

(defn get-by-id [id]
  (first (jdbc/query db-spec (str "select * from raw_device_log where id = " id))))

(defn create [params]
  (jdbc/insert! db-spec :raw_device_log params))
