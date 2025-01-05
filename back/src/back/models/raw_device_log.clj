(ns back.models.raw-device-log
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [clojure.core :refer [re-find re-matcher]]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def defaults
  {:limit 100
   :order [{:key "created_at" :dir "DESC"}]})

(defn filter-key [key]
  (when (.contains ["created_at" "id" "data"] key)
    key))

(defn filter-order-dir [dir]
  (when (.contains ["DESC" "desc" "ASC" "asc"] dir)
    dir))

(defn build-target-key [{:keys [key table-key] :as params}]
  #_(println "build-target-key" key table-key params)
  (let [fn-filter-key (or (:filter-key params) filter-key)
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
                  (model.util/escape-for-sql json-key)
                  (join "" (for [[index k] (map-indexed vector json-key)]
                             (if (number? k)
                               (str "[" k "]")
                               (str (when-not (= index 0) ".")
                                    (model.util/escape-for-sql k)))))))))))

(defn build-query-order [order table-key]
  #_(println "build-query-order " order)
  (let [order-to-handle (or (seq order) (:order defaults))]
    (str "ORDER BY "
         (join ", " (for [item order-to-handle]
                      (join " " [(build-target-key {:key (or (get item "key") (:key item))
                                                    :table-key table-key})
                                 (filter-order-dir (or (get item "dir") (:dir item)))]))))))

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

(defn build-query-item-where-not-or [params {:keys [base-table-key this-table-key]}]
  #_(println "build query item where " params base-table-key this-table-key)
  (let [action (get params "action")
        value (get params "value")
        key (get params "key")
        hours-action (when (string? action)
                       (-> (re-matcher #"(in|not-in)-hours-(\d+)$" action)
                           re-find
                           #_((fn [x] (println "parsing hours-action" x) x))
                           rest))
        target-key (build-target-key {:key key
                                      :table-key base-table-key})
        target-action (filter-target-action action)
        target-value (cond
                       (string? value) (str "\"" (model.util/escape-for-sql value) "\"")
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

(defn build-query-item-where [params {:keys [base-table-key this-table-key]}]
  #_(println "build query item where " params base-table-key this-table-key)
  (if-let [or-where (get params "or")]
    (->> (for [item or-where]
           (build-query-item-where item {:base-table-key base-table-key :this-table-key this-table-key}))
         (join " OR ")
         ((fn [query-or] (str "(" query-or ")"))))
    (build-query-item-where-not-or params {:base-table-key base-table-key :this-table-key this-table-key})))

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

(defn build-query-where [{:keys [where base-table-key str-where-and]}]
  (->>
   [str-where-and
    (when-not (or (nil? where) (empty? where))
      (let [where-max-group-by (filter where-max-group-by? where)
            where-normal (filter #(not (where-max-group-by? %)) where)]
        (join " AND "
              (filter seq
                      [(join " AND "
                             (for [[index item] (map-indexed vector where-max-group-by)]
                               #_(println "item" item)
                               (build-query-item-where-max-group-by index item {:base-table-key base-table-key})))
                       (join " AND "
                             (for [item where-normal]
                               #_(println "item" item)
                               (build-query-item-where item {:base-table-key base-table-key})))]))))]
   (filter seq)
   ((fn [and-list]
      (when-not (empty? and-list)
        (str "where " (join " AND " and-list)))))))

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

(defn get-list-with-total [params & [{:keys [str-where-and]}]]
  (let [limit (or (:limit params) (:limit defaults))
        order (when-let [str-order (:order params)]
                (json/read-str str-order))
        where (when-let [w (:where params)] (json/read-str w))
        db-table-key "raw_device_log"
        base-table-key "rdl"
        where-max-group-by (filter where-max-group-by? where)
        str-query-select-max-group-by
        (build-query-select-max-group-by where-max-group-by
                                         {:db-table-key db-table-key
                                          :base-table-key base-table-key})
        str-query (join " " ["SELECT SQL_CALC_FOUND_ROWS *, device.name device_name FROM" db-table-key "AS" base-table-key
                             "LEFT JOIN device ON device.id = rdl.device_id"
                             "LEFT JOIN device_group ON device_group.id = device.device_group_id"
                             (when-not (empty? str-query-select-max-group-by) (str ", " str-query-select-max-group-by))
                             (build-query-where {:where where
                                                 :base-table-key base-table-key
                                                 :str-where-and str-where-and})
                             (build-query-order order base-table-key)
                             "LIMIT " limit])]
    (println "str-query " str-query)
    (model.util/get-list-with-total [str-query])
    #_(jdbc/with-db-transaction [db-transaction db-spec]
        {:records (jdbc/query db-transaction str-query)
         :total (-> (jdbc/query db-transaction "SELECT FOUND_ROWS()") first vals first)})))

(defn get-by-id [id]
  (first (jdbc/query db-spec ["select * from raw_device_log where id = ?" id])))

(defn create [params]
  (jdbc/insert! db-spec :raw_device_log params))