(ns back.models.device-watch-group
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def name-table "device_watch_group")
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :owner_user_id :memo]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! db-spec key-table params ["id = ?" id])
    {key-table (get-by-id id {:transaction t-con})}))

(defn update-for-owner-user [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! db-spec key-table params ["id = ? AND owner_user_id = ?" id id-user])
    {key-table (get-by-id id {:transaction t-con})}))

(defn delete-for-owner-user [{:keys [id id-user]}]
  (jdbc/delete! db-spec key-table ["id = ? AND owner_user_id = ?" id id-user]))

(defn get-by-id-for-owner-user [id user-id & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     [(str "SELECT * FROM " name-table " WHERE id = ? AND owner_user_id = ?") id user-id])))

(defn create [params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/insert! t-con key-table (filter-params params))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      {key-table item})))

(defn- get-list-with-total-base [params & [{:keys [str-where]}]]
  (model.util/get-list-with-total-with-building-query name-table params {:str-where str-where}))

(defn get-list-with-total-for-owner-user [params user-id]
  (get-list-with-total-base params {:str-where (format "owner_user_id = %d" user-id)}))

(defn get-list-with-total-for-admin [params]
  (get-list-with-total-base params))
