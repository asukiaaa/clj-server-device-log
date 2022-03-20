(ns asuki.back.models.common
  (:require [clojure.java.jdbc :as jdbc]
            [asuki.back.config :refer [db-spec]]))


(defn init-db []
  ;; (jdbc/get-connection db-spec) ; check connection to db
  (let [table-info (jdbc/query db-spec "Show tables")
        tables (for [x table-info] (first (vals x)))]
    ;; (println table-info)
    ;; (println tables)
    (when-not (.contains tables "book")
      (println "required book table")
      (->> (jdbc/create-table-ddl
            :book {:id :serial
                   :name "varchar(100)"})
           (jdbc/db-do-commands db-spec)))
    (when-not (.contains tables "user")
      (println "required user table")
      (->> (jdbc/create-table-ddl
            :user {:id :serial
                   :name "varchar(100)"})
           (jdbc/db-do-commands db-spec)))
    (when-not (.contains tables "raw_device_log")
      (println "required raw_device_log table")
      (->> (jdbc/create-table-ddl
            :raw_device_log {:id :serial
                             :data "JSON NOT NULL"
                             :created_at "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"})
           (jdbc/db-do-commands db-spec)))))

(defn create-test-data []
  (let [users (jdbc/query db-spec "select * from user")]
    (println users)
    (when (= 0 (count users))
      (jdbc/insert-multi! db-spec :user (for [i (range 5)]
                                          {:name (str "hoi" i)})))))
