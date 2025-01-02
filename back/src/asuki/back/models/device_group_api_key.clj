(ns asuki.back.models.device-group-api-key
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.data.json :as json]
            [clojure.string :refer [join split]]
            [asuki.back.config :refer [db-spec]]
            [asuki.back.models.util :as model.util]
            [asuki.back.models.device-group :as model.device-group]))

(def name-table "device_group_api_key")
(def name-table-with-device-group (format "%s LEFT JOIN %s ON device_group_id = %s.id"
                                          name-table
                                          model.device-group/name-table
                                          model.device-group/name-table))
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:name :device_group_id :permission]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn get-by-id-for-user-and-device-group [id-device-group-api-key id-user id-device-group & [{:keys [transaction]}]]
  (let [query (join " " ["SELECT * from" name-table-with-device-group "WHERE"
                         (join " AND "
                               [(str "device_group.user_id = " id-user)
                                (str "device_group_id = " id-device-group)
                                (str "device_group_api_key.id = " id-device-group-api-key)])])]
    (first (jdbc/query (or transaction db-spec) [query]))))

(defn get-by-id-for-user [id-device-group-api-key id-user & [{:keys [transaction]}]]
  (let [query (join " " ["SELECT * from" name-table-with-device-group "WHERE"
                         (join " AND "
                               [(str "device_group.user_id = " id-user)
                                (str "device_group_api_key.id = " id-device-group-api-key)])])]
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

(defn create-for-user [params id-user]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [id-device-group (:device_group_id params)
          group (model.device-group/get-by-id-for-user id-device-group id-user {:transaction t-con})]
      (if (empty? group)
        {:errors ["Not found device group or no permission to create key for the device group"]}
        (let [key-str (model.util/build-random-str-alphabets-and-number 100)]
          (try
            (jdbc/insert! t-con key-table (-> params filter-params (assoc :key_str key-str)))
            (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                         first vals first)
                  item (get-by-id id {:transaction t-con})]
              {key-table item})
            (catch Exception ex
              {:errors (json/write-str {:__system [(.getMessage ex)]})})))))))

(defn- get-list-with-total-base [params & [{:keys [str-where]}]]
  (-> (model.util/build-query-get-index name-table-with-device-group)
      (#(if-not (empty? str-where) (str % " where " str-where) %))
      (model.util/append-limit-offset-by-limit-page-params params)
      model.util/get-list-with-total
      #_((fn [result]
           (clojure.pprint/pprint (:list result))
           (println (count (:list result)))
           result))))

(defn get-list-with-total-for-user-and-device-group [params id-user id-device-group]
  (get-list-with-total-base params {:str-where (format "%s.user_id = %d AND device_group_id = %d" model.device-group/name-table id-user id-device-group)}))

(defn get-by-key-post [key-post]
  (when-not (empty? key-post)
    (let [[key-device-group id-device-group key-device-group-api-key id-device-group-api-key key-str] (split key-post #":")
          device-group-api-key (when (and (= key-device-group "device_group")
                                          (= key-device-group-api-key "device_group_api_key"))
                                 (get-by-id id-device-group-api-key))]
      (when (and (= key-str (:key_str device-group-api-key))
                 (= id-device-group (str (:device_group_id device-group-api-key))))
        device-group-api-key))))

(defn has-permission-to-create-device [device-group-api-key]
  (let [permission (json/read-json (:permission device-group-api-key))]
    (:create_device permission)))
