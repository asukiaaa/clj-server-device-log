(ns back.models.device-log
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [clojure.core :refer [re-find re-matcher]]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [java-time.api :as java-time]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.models.util.device :as util.device]
            [back.models.util.device-log :as util.device-log]
            [back.models.util.device-type :as util.device-type]
            [back.util.time :refer [time-format-yyyymmdd-hhmmss]]))

(defn build-str-join-tables [& [name-table]]
  (let [name-table (or name-table util.device-log/name-table)]
    (join " " [(format "LEFT JOIN %s ON %s.id = %s.device_id"
                       util.device/name-table
                       util.device/name-table
                       name-table)
               (format "LEFT JOIN %s ON %s.id = %s.device_type_id"
                       util.device-type/name-table
                       util.device-type/name-table
                       util.device/name-table)])))

(defn build-str-keys-select-with-peripherals [& [name-table]]
  (let [name-table (or name-table util.device-log/name-table)]
    (format "%s.*, %s, %s"
            name-table
            (util.device/build-str-select-params-for-joined)
            (util.device-type/build-str-select-params-for-joined))))

(defn build-item [item]
  (-> item
      util.device/build-item-from-selected-params-joined
      util.device-type/build-item-from-selected-params-joined
      ((fn [item]
         (let [device-type (util.device-type/key-table item)]
           (-> (dissoc item util.device-type/key-table)
               (assoc-in [util.device/key-table util.device-type/key-table] device-type)))))
      #_((fn [item] (println item) item))))

(def defaults
  {:limit 100
   :order [{:key "created_at" :dir "DESC"}]})

(defn filter-key [key]
  (when (.contains ["created_at" "device_id" "id" "data"] key)
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
    (join ", " (for [item order-to-handle]
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
                   (format "\"%s\""
                           (java-time/format time-format-yyyymmdd-hhmmss
                                             (java-time/minus (java-time/local-date-time) (java-time/hours (Integer. str-hours)))))]))
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
        base-key-max (build-target-key (-> (walk/keywordize-keys (get item "max"))
                                           (assoc :table-key base-table-key)))
        target-key-group-by (build-target-key {:key (:group-by keys)
                                               :table-key max-group-by-table-key
                                               :filter-key (fn [x] x)})
        base-key-group-by (build-target-key (walk/keywordize-keys (get item "group_by")))]
    #_(println target-key-max base-key-max target-key-group-by base-key-group-by)
    (join " " [target-key-max "=" base-key-max "AND" target-key-group-by "=" base-key-group-by])))

(defn where-max-group-by? [item]
  (or (get item "group_by") (get item "max")))

(defn build-query-where [{:keys [where base-table-key build-str-where-and]}]
  (->>
   [(when build-str-where-and (build-str-where-and base-table-key))
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
        (join " AND " and-list))))))

(defn build-query-select-max-group-by [where-max-group-by {:keys [name-table base-table-key]}]
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
                                               "max(" (build-target-key (walk/keywordize-keys (get item "max")))
                                               ") AS" converted-max-key ","
                                               group-by-key "AS" converted-group-by-key
                                               "FROM" name-table
                                               "GROUP BY" group-by-key
                                               ") as" table-key])]
                          query)))]
      (println "query for select-max-group-by" query)
      query)))

(defn get-list-with-total [params & [{:keys [build-str-join build-str-where-and transaction]}]]
  (let [limit (or (:limit params) (:limit defaults))
        page (or (:page params) 0)
        order (when-let [str-order (:order params)]
                (json/read-str str-order))
        where (when-let [w (:where params)] (json/read-str w))
        name-table util.device-log/name-table
        base-table-key "dl"
        where-max-group-by (filter where-max-group-by? where)
        str-query-select-max-group-by
        (build-query-select-max-group-by where-max-group-by
                                         {:name-table name-table
                                          :base-table-key base-table-key})
        str-query (join " " ["SELECT SQL_CALC_FOUND_ROWS dl.*, device.name device_name FROM" name-table "AS" base-table-key
                             "LEFT JOIN device ON device.id = dl.device_id"
                             "LEFT JOIN device_type ON device_type.id = device.device_type_id"
                             (when build-str-join (build-str-join base-table-key))
                             (when-not (empty? str-query-select-max-group-by) (str ", " str-query-select-max-group-by))
                             "WHERE"
                             (build-query-where {:where where
                                                 :base-table-key base-table-key
                                                 :build-str-where-and build-str-where-and})
                             "ORDER BY"
                             (build-query-order order base-table-key)
                             "LIMIT" limit
                             "OFFSET" (* limit page)])]
    #_(model.util/get-list-with-total-with-building-query
       util.device-log/name-table params
       {:str-keys-select (build-str-keys-select-with-peripherals base-table-key)
        :str-before-where (->> [(build-str-join-tables base-table-key)
                                str-query-select-max-group-by]
                               (remove nil?)
                               (join ", "))
        :str-order (build-query-order order base-table-key)
        :str-where (build-query-where {:where where
                                       :base-table-key base-table-key
                                       :build-str-where-and build-str-where-and})
        :build-item build-item
        :transaction transaction})
    #_(println "str-query" str-query transaction)
    (model.util/get-list-with-total [str-query] {:transaction transaction})))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id
   id util.device-log/name-table
   {:transaction transaction
    :str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-tables)
    :build-item build-item}))

(defn create [params]
  (jdbc/insert! db-spec util.device-log/key-table params))
