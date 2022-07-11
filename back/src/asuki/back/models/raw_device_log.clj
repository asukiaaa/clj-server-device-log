(ns asuki.back.models.raw-device-log
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join escape]]
            [clojure.core :refer [re-find re-matcher]]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
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

(defn build-target-key [{:keys [key table-key] :as args}]
  #_(println "build-target-key" key table-key args)
  (let [fn-filter-key (or (:filter-key args) filter-key)
        record-key (if (string? key) key (first key))
        json-key (when-not (string? key) (rest key))
        escaped-key (fn-filter-key record-key)]
    (when-let [key-with-table (when-not (nil? escaped-key)
                                (str (when table-key (str table-key ".")) escaped-key))]
      (if (empty? json-key)
        key-with-table
        (format "JSON_VALUE(%s,\"$.%s\")"
                key-with-table
                (if (string? json-key)
                  (escape-for-sql json-key)
                  (join "" (for [[index k] (map-indexed vector json-key)]
                             (if (number? k)
                               (str "[" k "]")
                               (str (when-not (= index 0) ".")
                                    (escape-for-sql k)))))))))))

(defn build-query-order [order table-key]
  #_(println "build-query-order " order)
  (str "ORDER BY "
       (join ", " (for [item order]
                    (join " " [(build-target-key {:key (or (get item "key") (:key item))
                                                  :table-key table-key})
                               (filter-order-dir (or (get item "dir") (:dir item)))])))))

(defn filter-target-action [action]
  (case action
    "eq" "="
    "=" "="
    "ne" "!="
    "!=" "!="
    "gt" ">"
    ">" ">"
    "gte" ">="
    ">=" ">="
    "lt" "<"
    "<" "<"
    "lte" "<="
    "<=" "<="
    "not_null" "IS NOT NULL"
    nil))

(defn build-query-item-where [args {:keys [base-table-key this-table-key]}]
  #_(println "build query item where " args base-table-key this-table-key)
  (let [action (get args "action")
        value (get args "value")
        key (get args "key")
        hours-action (when (string? action)
                       (-> (re-matcher #"(in|not-in)-hours-(\d+)$" action)
                           re-find
                           #_((fn [x] (println "parsing hours-action" x) x))
                           rest))
        target-key (build-target-key {:key key
                                      :table-key base-table-key})
        target-action (filter-target-action action)
        target-value (cond
                       (string? value) (str "\"" (escape-for-sql value) "\"")
                       (not (nil? this-table-key))
                       (build-target-key {:key key
                                          :table-key this-table-key})
                       :else value)]
    #_(println "build-query-item-where" target-key target-action target-value base-table-key this-table-key not-exists)
    (cond
      (seq hours-action)
      (join " " (let [[in-or-not-in str-hours] hours-action]
                  [target-key (if (= in-or-not-in "in") ">=" "<")
                   (f/unparse (f/formatter "\"YYYY-MM-dd HH:mm:ss\"")
                              (t/minus (t/now) (t/hours (Integer. str-hours))))]))
      :else (join " " [target-key target-action target-value]))))

(defn build-keys-for-where-max-group-by [base-table-key index]
  #_(println "build-keys-for-where-max-group-by" base-table-key index)
  {:table (str base-table-key "_where_max_group_" index)
   :max (str "val_max_" index)
   :group-by (str "val_group_by_" index)})

(defn build-query-item-where-max-group-by [index item {:keys [base-table-key]}]
  #_(println "build-query-item-where-max-group-by" index item base-table-key)
  (let [keys (build-keys-for-where-max-group-by base-table-key index)
        max-group-by-table-key (:table keys)
        target-key-max (build-target-key {:key (:max keys)
                                          :table-key max-group-by-table-key
                                          :filter-key (fn [x] x)})
        base-key-max (build-target-key (walk/keywordize-keys (get item "max")))
        target-key-group-by (build-target-key {:key (:group-by keys)
                                               :table-key max-group-by-table-key
                                               :filter-key (fn [x] x)})
        base-key-group-by (build-target-key (walk/keywordize-keys (get item "group_by")))]
    #_(println target-key-max base-key-max target-key-group-by base-key-group-by)
    (join " " [target-key-max "=" base-key-max "AND" target-key-group-by "=" base-key-group-by])))

(defn where-max-group-by? [item]
  (or (get item "group_by") (get item "max")))

(defn build-query-where [{:keys [where base-table-key]}]
  (when-not (or (nil? where) (empty? where))
    #_(println "where" where)
    (let [where-max-group-by (filter where-max-group-by? where)
          where-normal (filter #(not (where-max-group-by? %)) where)]
      (str "WHERE "
           (join " AND "
                 (filter seq
                         [(join " AND "
                                (for [[index item] (map-indexed vector where-max-group-by)]
                                  #_(println "item" item)
                                  (build-query-item-where-max-group-by index item {:base-table-key base-table-key})))
                          (join " AND "
                                (for [item where-normal]
                                  #_(println "item" item)
                                  (build-query-item-where item {:base-table-key base-table-key})))]))))))

(defn build-query-select-max-group-by [where-max-group-by {:keys [db-table-key base-table-key]}]
  #_(println "build-query-select-max-group-by" where-max-group-by)
  (when-not (empty? where-max-group-by)
    (let [;; table-keys (for [item where-max-group-by] (str ))
          query (join ", "
                      (for [[index item] (map-indexed vector where-max-group-by)]
                        (let [keys-max-group-by (build-keys-for-where-max-group-by base-table-key index)
                              table-key (:table keys-max-group-by)
                              converted-max-key (:max keys-max-group-by)
                              converted-group-by-key (:group-by keys-max-group-by)
                              group-by-key (build-target-key (walk/keywordize-keys (get item "group_by")))
                              query (join " " ["(SELECT"
                                               "max(" (build-target-key (walk/keywordize-keys (get item "max"))) ") AS" converted-max-key ","
                                               group-by-key "AS" converted-group-by-key
                                               "FROM" db-table-key
                                               "GROUP BY" group-by-key
                                               ") as" table-key])]
                          query)))]
      (println "query for select-max-group-by" query)
      query)))

(defn get-records-with-total [& [args]]
  (println "get-records-with-total" args)
  (let [limit (or (:limit args) (:limit defaults))
        order (or (when-let [str-order (:order args)]
                    (json/read-str str-order))
                  (:order defaults))
        where (json/read-str (:where args))
        db-table-key "raw_device_log"
        base-table-key "rdl"
        where-max-group-by (filter where-max-group-by? where)
        str-query-select-max-group-by
        (build-query-select-max-group-by where-max-group-by
                                         {:db-table-key db-table-key
                                          :base-table-key base-table-key})
        str-query (join " " ["SELECT SQL_CALC_FOUND_ROWS * FROM" db-table-key "AS" base-table-key
                             (when-not (empty? str-query-select-max-group-by) (str ", " str-query-select-max-group-by))
                             (build-query-where {:where where
                                                 :base-table-key base-table-key})
                             (build-query-order order base-table-key)
                             "LIMIT " limit])]
    (println "str-query " str-query)
    (jdbc/with-db-transaction [db-transaction db-spec]
      {:records (jdbc/query db-transaction str-query)
       :total (-> (jdbc/query db-transaction "SELECT FOUND_ROWS()") first vals first)})))

(defn get-by-id [id]
  (first (jdbc/query db-spec (str "select * from raw_device_log where id = " id))))

(defn create [params]
  (jdbc/insert! db-spec :raw_device_log params))
