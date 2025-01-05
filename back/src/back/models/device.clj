(ns back.models.device
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [split]]
            [back.models.device-group :as model.device-group]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def name-table "device")
(def key-table (keyword name-table))
(def str-sql-select-for-device-group-columns "device_group.name device_group_name, device_group.user_id device_group_user_id")
(defn build-item-for-device-group [item]
  (when-not (empty? item)
    (let [device_group {:id (:device_group_id item) :name (:device_group_name item) :user_id (:device_group_user_id item)}
          item (assoc item :device_group device_group)]
      item)))

(defn filter-params [params]
  (select-keys params [:name :device_group_id :hash_post]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn delete [id]
  ; TODO prohibit deleting when who has device
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec key-table params ["id = ?" id]))

(defn get-by-id-for-user [id user-id & [{:keys [transaction]}]]
  (let [item (first (jdbc/query (or transaction db-spec)
                                [(format "SELECT device.*, %s FROM device INNER JOIN device_group ON device_group.id = device_group_id WHERE device.id = ? AND device_group.user_id = ?"
                                         str-sql-select-for-device-group-columns)
                                 id user-id]))
        item (build-item-for-device-group item)]
    #_(println item)
    item))

(defn for-user-update [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [item (get-by-id-for-user id id-user {:transaction t-con})]
      (if (empty? item)
        {:errors ["item not found"]}
        (do
          (jdbc/update! db-spec key-table params ["id = ?" id])
          {:device (get-by-id id {:transaction t-con})})))))

(defn for-user-delete [{:keys [id id-user]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [item (get-by-id-for-user id id-user {:transaction t-con})]
      (if (empty? item)
        {:errors ["device does not avairable"]}
        (do
          (jdbc/delete! db-spec key-table ["id = ?" id])
          {})))))

(defn assign-hash-post-to-params [params]
  (assoc params :hash_post (model.util/build-random-str-alphabets-and-number 40)))

(defn create [params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/insert! t-con key-table (-> params assign-hash-post-to-params filter-params))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      {:device item})))

(defn create-for-user [params user-id]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [device-group-id (:device_group_id params)
          device-group (model.device-group/get-by-id-for-user device-group-id user-id {:transaction t-con})]
      (if (empty? device-group)
        {:errors ["device group does not avairable"]}
        (do
          (jdbc/insert! t-con key-table (-> params
                                            assign-hash-post-to-params
                                            filter-params))
          (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                       first vals first)
                item (get-by-id id {:transaction t-con})]
            {:device item}))))))

(defn get-list-with-total-for-user [params user-id]
  (-> (model.util/build-query-get-index name-table {:str-keys-select (str "device.*, " str-sql-select-for-device-group-columns)})
      (str " INNER JOIN device_group ON device_group_id = device_group.id")
      (str (format " WHERE device_group.user_id = %d" user-id))
      (model.util/append-limit-offset-by-limit-page-params params)
      (model.util/get-list-with-total {:build-item build-item-for-device-group})))

(defn get-list-with-total-for-admin [params]
  (model.util/get-list-with-total-with-building-query name-table params))

(defn get-by-key-post [key-post]
  (when-not (nil? key-post)
    (let [[key id hash] (split key-post #":")
          device (when (= key "device")
                   (get-by-id id))]
      (when (= hash (:hash_post device))
        device))))

(defn build-key-post [device]
  (str "device:" (:id device) ":" (:hash_post device)))
