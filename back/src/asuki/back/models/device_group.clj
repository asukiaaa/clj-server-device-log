(ns asuki.back.models.device-group
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [asuki.back.config :refer [db-spec]]
            [asuki.back.models.util :as model.util]))

(def name-table "device_group")
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :user_id]))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec key-table params ["id = ?" id]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn get-by-id-for-user [id user-id & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     ["SELECT * FROM device_group WHERE id = ? AND user_id = ?" id user-id])))

(defn create [params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/insert! t-con key-table (filter-params params))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      {:device_group item})))

(defn get-list-with-total-for-user [params user-id]
  (-> (model.util/build-query-get-index name-table)
      (str (format " where user_id = %d" user-id))
      (model.util/append-limit-offset-by-limit-page-params params)
      model.util/get-list-with-total))

(defn get-list-with-total-for-admin [params]
  (-> (model.util/build-query-get-index name-table)
      (model.util/append-limit-offset-by-limit-page-params params)
      model.util/get-list-with-total))
