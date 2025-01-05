(ns back.models.device-group
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [split]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def name-table "device_group")
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :user_id]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn delete [id]
  ; TODO prohibit deleting when who has device
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec key-table params ["id = ?" id]))

(defn for-user-update [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! db-spec key-table params ["id = ? AND user_id = ?" id id-user])
    {:device_group (get-by-id id {:transaction t-con})}))

(defn for-user-delete [{:keys [id id-user]}]
  ; TODO prohibit deleting when who has device
  (jdbc/delete! db-spec key-table ["id = ? AND user_id = ?" id id-user]))

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

(defn- get-list-with-total-base [params & [{:keys [str-where]}]]
  (model.util/get-list-with-total-with-building-query name-table params {:str-where str-where}))

(defn get-list-with-total-for-user [params user-id]
  (get-list-with-total-base params {:str-where (format "user_id = %d" user-id)}))

(defn get-list-with-total-for-admin [params]
  (get-list-with-total-base params))

(defn get-by-key-post [key-post]
  (when-not (nil? key-post)
    (let [[key id hash] (split key-post #":")
          device (when (= key "device_group")
                   (get-by-id id))]
      (when (= hash (:hash_post device))
        device))))
