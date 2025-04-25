(ns back.models.device-type-api-key
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.util.encryption :as encryption]
            [back.models.util.user-team-permission :as util.user-team-permission]
            [back.models.util.device-type :as util.device-type]))

(def name-table "device_type_api_key")
(def key-table (keyword name-table))

(defn build-str-keys-select-with-peripherals []
  (format "%s.*, %s"
          name-table
          (util.device-type/build-str-select-params-for-joined)))
(defn build-str-join-table []
  (format "%s LEFT JOIN %s ON device_type_id = %s.id"
          name-table
          util.device-type/name-table
          util.device-type/name-table))
(defn build-item [item]
  (-> item
      (util.device-type/build-item-from-selected-params-joined)))

(defn filter-params [params]
  (select-keys params [:name :device_type_id :permission]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn build-authorization-bearer-for-item [item]
  (model.util/build-authorization-bearer (:key_str item) key-table :key_str))

(defn get-authorizaton-bearer-by-id [id-device & [{:keys [transaction]}]]
  (let [item (get-by-id id-device {:transaction transaction})]
    (build-authorization-bearer-for-item item)))

(defn get-by-key-str [key-str & [{:keys [transaction]}]]
  (when-not (empty? key-str)
    (let [query (format "SELECT * from %s WHERE key_str = \"%s\"" name-table (model.util/escape-for-sql key-str))]
      (first (jdbc/query (or transaction db-spec) [query])))))

(defn get-by-authorization-bearer [bearer & [{:keys [transaction]}]]
  (let [decoded-bearer (encryption/decode bearer)
        key-str (-> decoded-bearer key-table :key_str)]
    (get-by-key-str key-str {:transaction transaction})))

(defn build-str-where-ids-user-team [sql-ids-user-team]
  (format "%s.%s_id IN %s"
          util.device-type/name-table
          util.device-type/name-manager-user-team
          sql-ids-user-team))

(defn build-str-where-for-user-and-device-type [id-user id-device-type]
  (->> [(build-str-where-ids-user-team
         (util.user-team-permission/build-query-ids-for-user-write id-user))
        (format "%s.device_type_id = %d"
                name-table id-device-type)]
       (join " AND ")))

(defn get-by-id-for-user-and-device-type [id-device-type-api-key id-user id-device-type & [{:keys [transaction]}]]
  (model.util/get-by-id
   id-device-type-api-key name-table
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-table)
    :str-where (build-str-where-for-user-and-device-type id-user id-device-type)
    :build-item build-item
    :transaction transaction})
  #_(let [query (join " " ["SELECT * from" name-table-with-device-type "WHERE"
                           (join " AND "
                                 [(str "device_type.user_id = " id-user)
                                  (str "device_type_id = " id-device-type)
                                  (str "device_type_api_key.id = " id-device-type-api-key)])])]
      (first (jdbc/query (or transaction db-spec) [query]))))

(defn get-by-id-for-ids-user-team [id-device-type-api-key sql-ids-user-team & [{:keys [transaction]}]]
  (model.util/get-by-id
   id-device-type-api-key name-table
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-table)
    :str-where (build-str-where-ids-user-team sql-ids-user-team)
    :build-item build-item
    :transaction transaction})
  #_(let [query (join " " ["SELECT * from" name-table-with-device-type "WHERE"
                           (join " AND "
                                 [(str "device_type.user_team_id IN " sql-ids-user-team)
                                  (str "device_type_api_key.id = " id-device-type-api-key)])])]
      (first (jdbc/query (or transaction db-spec) [query]))))

(defn get-by-id-for-user [id-device-type-api-key id-user & [{:keys [transaction]}]]
  (get-by-id-for-ids-user-team
   id-device-type-api-key
   (util.user-team-permission/build-query-ids-for-user-show id-user)
   (:transaction transaction)))

(defn get-by-id-for-user-to-write [id-device-type-api-key id-user & [{:keys [transaction]}]]
  (get-by-id-for-ids-user-team
   id-device-type-api-key
   (util.user-team-permission/build-query-ids-for-user-write id-user)
   (:transaction transaction)))

(defn update [id params]
  (model.util/update key-table id params))

(defn update-for-user [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (when-let [_ (get-by-id-for-user id id-user {:transaction t-con})]
      (jdbc/update! t-con key-table params ["id = ?" id])
      (let [item (get-by-id-for-user id id-user {:transaction t-con})]
        item))))

(defn delete [id]
  (model.util/delete name-table id))

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

(defn create [params & [{:keys [transaction]}]]
  (let [key-str (build-unique-key-str transaction)]
    (model.util/create key-table (-> params filter-params (assoc :key_str key-str))
                       {:transaction transaction})))

(defn create-for-user [params id-user & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (let [id-device-type (:device_type_id params)
          query (format "SELECT %d in %s"
                        id-device-type
                        (util.user-team-permission/build-query-ids-for-user-write id-user))
          is-writable (-> (jdbc/query transaction query) first vals first (> 0))]
      (if-not is-writable
        {:errors (json/write-str
                  {:__system [(format "Not found %s or no permission to create %s"
                                      util.device-type/name-table
                                      name-table)]})}
        (create params {:transaction transaction})))))

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-table)
    :str-where str-where
    :build-item build-item
    :transaction transaction}))

(defn get-list-with-total-for-device-type [params id-device-type & [{:keys [transaction]}]]
  (get-list-with-total-base params {:str-where (format "device_type_id = %d" id-device-type)
                                    :transaction transaction}))

(defn has-permission-to-create-device? [device-type-api-key]
  (let [permission (json/read-json (:permission device-type-api-key))]
    (:create_device permission)))
