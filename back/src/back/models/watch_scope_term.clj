(ns back.models.watch-scope-term
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]))

(def name-table "watch_scope_term")
#_(def name-table-with-device (str name-table " LEFT JOIN device ON device.id = device_id"))
(def key-table (keyword name-table))
#_(def str-keys-select-with-device-name (str name-table ".*, device.name as device_name"))

(defn filter-params [params]
  (select-keys params [:id :device_id :watch_scope_id :datetime_from :datetime_until]))

(defn build-query-device-ids-for-watch-scope [id-watch-scope]
  (format "(SELECT device_id from %s WHERE watch_scope_id = %s)"
          name-table
          id-watch-scope))

(defn filter-params [params]
  (select-keys params [:device_id :watch_scope_id :from :until]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table
                        {:transaction transaction
                         ;:str-keys-select str-keys-select-with-device-name
                         :str-key-id (str name-table ".id")}))

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

(defn create-list-for-watch-scope [terms id-watch-scope & [{:keys [transaction]}]]
  (let [params (for [term terms] (-> term filter-params (assoc :watch_scope_id id-watch-scope)))]
    (jdbc/insert-multi! (or transaction db-spec) key-table params)))

(defn get-list-for-watch-scope [id-watch-scope & [{:keys [transaction]}]]
  (jdbc/query (or transaction db-spec)
              (format "SELECT * FROM %s WHERE watch_scope_id = %d"
                      name-table
                      id-watch-scope)))

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-where str-where
    ;:str-keys-select str-keys-select-with-device-name
    :transaction transaction}))

#_(defn get-list-with-total-for-user-team [params user-team-id & [{:keys [transaction]}]]
    (get-list-with-total-base params {:str-where (format "user_team_id = %d" user-team-id)
                                      :transaction transaction}))

(defn get-list-with-total [params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction}))
