(ns back.models.device
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.models.device-type :as model.device-type]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def name-table "device")
(def key-table (keyword name-table))
(def str-sql-select-for-device-type-columns "device_type.name device_type_name, device_type.user_id device_type_user_id")
(def str-sql-select-for-user-team-columns "user_team.name user_team_name, user_team.owner_user_id user_team_owner_user_id")
(def str-join-tables
  (join " " ["INNER JOIN device_type ON device_type_id = device_type.id"
             "LEFT JOIN user_team ON user_team_id = user_team.id"]))

(defn build-item-for-device-type [item]
  (if (empty? item)
    item
    (let [device_type {:id (:device_type_id item)
                        :name (:device_type_name item)
                        :user_id (:device_type_user_id item)}
          item (assoc item :device_type device_type)]
      item)))

(defn build-item-for-user-team [item]
  (if (or (empty? item) (nil? (:user_team_id item)))
    item
    (let [user-team {:id (:user_team_id item)
                     :name (:user_team_name item)
                     :owner_user_id (:user_team_owner_user_id item)}
          item (assoc item :user_team user-team)]
      item)))

(defn build-item [item]
  (-> item
      build-item-for-device-type
      build-item-for-user-team))

(defn filter-params [params]
  (select-keys params [:name :device_type_id :user_team_id]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn get-by-hash-post [hash-post & [{:keys [transaction]}]]
  (when-not (empty? hash-post)
    (let [query (format "SELECT * from %s WHERE hash_post = \"%s\"" name-table (model.util/escape-for-sql hash-post))
          user (first (jdbc/query (or transaction db-spec) [query]))]
      user)))

(defn delete [id]
  ; TODO prohibit deleting when who has device
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec key-table params ["id = ?" id]))

(defn get-by-id-for-user [id user-id & [{:keys [transaction]}]]
  (let [query (format "SELECT device.*, %s, %s FROM device %s WHERE device.id = ? AND device_type.user_id = ?"
                      str-sql-select-for-device-type-columns
                      str-sql-select-for-user-team-columns
                      str-join-tables)
        item (first (jdbc/query (or transaction db-spec) [query id user-id]))
        item (build-item item)]
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

(defn build-hash-post []
  (model.util/build-random-str-alphabets-and-number 60))

(defn assign-unique-hash-post-to-params [params transaction]
  (let [hash (build-hash-post)
        user (get-by-hash-post hash {:transaction transaction})]
    (if (empty? user)
      (assoc params :hash_post hash)
      (assign-unique-hash-post-to-params params transaction))))

(defn create [params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/insert! t-con key-table (-> params filter-params (assign-unique-hash-post-to-params t-con)))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      {:device item})))

(defn create-for-user [params user-id]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [device-type-id (:device_type_id params)
          device-type (model.device-type/get-by-id-for-user device-type-id user-id {:transaction t-con})]
      (if (empty? device-type)
        {:errors ["device group does not avairable"]}
        (do
          (jdbc/insert! t-con key-table (-> params
                                            filter-params
                                            (assign-unique-hash-post-to-params t-con)))
          (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                       first vals first)
                item (get-by-id id {:transaction t-con})]
            {:device item}))))))

(defn get-list-with-total-for-user [params user-id]
  (-> (model.util/build-query-get-index
       name-table {:str-keys-select (str "device.*, " str-sql-select-for-device-type-columns
                                         ", " str-sql-select-for-user-team-columns)})
      (str " " str-join-tables)
      (str (format " WHERE device_type.user_id = %d" user-id))
      (model.util/append-limit-offset-by-limit-page-params params)
      (model.util/get-list-with-total {:build-item build-item})))

(defn get-list-with-total-for-admin [params]
  (model.util/get-list-with-total-with-building-query name-table params))
