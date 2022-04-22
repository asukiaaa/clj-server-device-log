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

(defn filter-keys [key]
  (when [contains? ["created_at" "id" "data"] key]
    key))

(defn filter-order-dir [dir]
  (when [contains? ["DESC" "desc" "ASC" "asc"] dir]
    dir))

(defn build-target-key [{:keys [key json-key]}]
  (let [escaped-key (filter-keys key)]
    (if (nil? json-key)
      escaped-key
      (format "JSON_VALUE(%s,\"$.%s\")"
              escaped-key
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

(defn build-query-item-where [args]
  #_(println "build query item where " args)
  (let [action (get args "action")
        value (get args "value")
        str-hours-from-action (-> (re-matcher #"in-hours-(\d+)$" action)
                                  re-find
                                  second)
        target-key (build-target-key {:key (get args "key") :json-key (get args "json_key")})
        target-action (case action
                        "=" "="
                        "gt" ">"
                        "gte" ">="
                        "lt" "<"
                        "lte" "<="
                        nil)
        target-value (if (string? value) (str "\"" (escape-for-sql value) "\"")
                         value)]
    #_(println (join " " ["build-query-item-where" target-key target-action target-value]))
    (cond
      (not (nil? str-hours-from-action))
      (join " " [target-key ">"
                 (f/unparse (f/formatter "\"YYYY-MM-dd HH:mm:ss\"")
                            (t/minus (t/now) (t/hours (Integer. str-hours-from-action))))])
      :else (join " " [target-key target-action target-value]))))

(defn build-query-where [where]
  (when-not (or (nil? where) (empty? where))
    #_(println "where" where)
    (str "WHERE "
         (join ", " (for [item (json/read-str where)]
                      #_(println "item" item)
                      (build-query-item-where item))))))

(defn get-all [& [args]]
  (println "get-all" args)
  (let [limit (or (:limit args) (:limit defaults))
        order (or (when-let [str-order (:order args)]
                    (json/read-str str-order))
                  (:order defaults))
        str-query (join " " ["select * from raw_device_log"
                             (build-query-where (:where args))
                             (build-query-order order)
                             "limit" limit])]
    (println "str-query " str-query)
    (jdbc/query db-spec str-query)))

(defn get-count-all [& [args]]
  (let [str-query  (join " " ["select COUNT(*) from raw_device_log"
                              (build-query-where (:where args))])]
    (println "get-count-all str-query " str-query)
    (let [count-all (-> (jdbc/query db-spec str-query) first vals first)]
      count-all)))

(defn get-by-id [id]
  (first (jdbc/query db-spec (str "select * from raw_device_log where id = " id))))

(defn create [params]
  (jdbc/insert! db-spec :raw_device_log params))
