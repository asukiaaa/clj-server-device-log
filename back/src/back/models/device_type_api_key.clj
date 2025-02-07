(ns back.models.device-type-api-key
  (:refer-clojure :exclude [update])
  (:require [clj-time.core :as cljt]
            [clj-time.format :as cljt-format]
            [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.device-type :as model.device-type]
            [back.models.util :as model.util]
            [back.util.encryption :as encryption]))

(def name-table "device_type_api_key")
(def name-table-with-device-type
  (format "%s LEFT JOIN %s ON device_type_id = %s.id"
          name-table
          model.device-type/name-table
          model.device-type/name-table))
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :device_type_id :permission]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn build-authorization-bearer [item]
  (let [data-for-bearer {key-table {:key_str (:key_str item)}
                         :created_at (cljt-format/unparse model.util/time-format-yyyymmdd-hhmmss (cljt/now))}]
    (encryption/encode data-for-bearer)))

(defn get-authorizaton-bearer-by-id [id-device & [{:keys [transaction]}]]
  (let [item (get-by-id id-device {:transaction transaction})]
    (build-authorization-bearer item)))

(defn get-by-key-str [key-str & [{:keys [transaction]}]]
  (when-not (empty? key-str)
    (let [query (format "SELECT * from %s WHERE key_str = \"%s\"" name-table (model.util/escape-for-sql key-str))]
      (first (jdbc/query (or transaction db-spec) [query])))))

(defn get-by-authorization-bearer [bearer & [{:keys [transaction]}]]
  (let [decoded-bearer (encryption/decode bearer)
        key-str (-> decoded-bearer key-table :key_str)]
    (get-by-key-str key-str {:transaction transaction})))

(defn get-by-id-for-user-and-device-type [id-device-type-api-key id-user id-device-type & [{:keys [transaction]}]]
  (let [query (join " " ["SELECT * from" name-table-with-device-type "WHERE"
                         (join " AND "
                               [(str "device_type.user_id = " id-user)
                                (str "device_type_id = " id-device-type)
                                (str "device_type_api_key.id = " id-device-type-api-key)])])]
    (first (jdbc/query (or transaction db-spec) [query]))))

(defn get-by-id-for-user [id-device-type-api-key id-user & [{:keys [transaction]}]]
  (let [query (join " " ["SELECT * from" name-table-with-device-type "WHERE"
                         (join " AND "
                               [(str "device_type.user_id = " id-user)
                                (str "device_type_api_key.id = " id-device-type-api-key)])])]
    (first (jdbc/query (or transaction db-spec) [query]))))

(defn update-for-user [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (when-let [_ (get-by-id-for-user id id-user {:transaction t-con})]
      (jdbc/update! t-con key-table params ["id = ?" id])
      (let [item (get-by-id-for-user id id-user {:transaction t-con})]
        {key-table item}))))

(defn delete-for-user [{:keys [id id-user]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (when-let [_ (get-by-id-for-user id id-user {:transaction t-con})]
      (jdbc/delete! t-con name-table ["id = ?" id]))))

(defn build-key-str []
  (model.util/build-random-str-alphabets-and-number 100))

(defn build-unique-key-str [transaction]
  (let [key-str (build-key-str)
        user (get-by-key-str key-str [:transaction transaction])]
    (if (empty? user)
      key-str
      (build-unique-key-str transaction))))

(defn create-for-user [params id-user]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [id-device-type (:device_type_id params)
          group (model.device-type/get-by-id-for-user id-device-type id-user {:transaction t-con})]
      (if (empty? group)
        {:errors ["Not found device group or no permission to create key for the device group"]}
        (let [key-str (build-unique-key-str t-con)]
          (try
            (jdbc/insert! t-con key-table (-> params filter-params (assoc :key_str key-str)))
            (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                         first vals first)
                  item (get-by-id id {:transaction t-con})]
              {key-table item})
            (catch Exception ex
              {:errors (json/write-str {:__system [(.getMessage ex)]})})))))))

(defn- get-list-with-total-base [params & [{:keys [str-where]}]]
  (model.util/get-list-with-total-with-building-query name-table-with-device-type params {:str-where str-where}))

(defn get-list-with-total-for-user-and-device-type [params id-user id-device-type]
  (get-list-with-total-base params {:str-where (format "%s.user_id = %d AND device_type_id = %d" model.device-type/name-table id-user id-device-type)}))

(defn has-permission-to-create-device [device-type-api-key]
  (let [permission (json/read-json (:permission device-type-api-key))]
    (:create_device permission)))
