(ns back.models.watch-scope-term
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util.device-log :as util.device-log]
            [back.models.util :as model.util]
            [back.models.util.device :as util.device]))

(def name-table "watch_scope_term")
(def key-table (keyword name-table))

(defn filter-params [params]
  (select-keys params [:id :device_id :watch_scope_id :datetime_from :datetime_until]))

(defn build-query-ids-device-log-for-watch-scope [id-watch-scope]
  (->> [(format "SELECT %s.id FROM %s INNER JOIN %s ON %s.device_id = %s.device_id"
                util.device-log/name-table
                util.device-log/name-table
                name-table
                util.device-log/name-table
                name-table)
        "WHERE"
        (format "%s.watch_scope_id = %d" name-table id-watch-scope)
        "AND"
        (format "(((%s.datetime_from IS NULL) OR (%s.datetime_from < %s.created_at)) AND ((%s.datetime_until IS NULL) OR (%s.datetime_until > %s.created_at)))"
                name-table
                name-table
                util.device-log/name-table
                name-table
                name-table
                util.device-log/name-table)
        (format "GROUP BY %s.id" util.device-log/name-table)]
       (join " ")
       (format "(%s)")))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table
                        {:transaction transaction
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

(defn delete-list-for-watch-scope [id-watch-scope & [{:keys [transaction]}]]
  (println :delete-list-for-watch-scoep id-watch-scope)
  (jdbc/delete! (or transaction db-spec) key-table ["watch_scope_id = ?" id-watch-scope]))

(defn create-list-for-watch-scope [id-watch-scope terms & [{:keys [transaction]}]]
  (let [params (for [term terms] (-> term filter-params (assoc :watch_scope_id id-watch-scope)))]
    (jdbc/insert-multi! (or transaction db-spec) key-table params)))

(defn get-list-with-device [{:keys [str-where transaction]}]
  (println :get-list-with-device str-where)
  (let [query
        (->> [(format "SELECT %s.*, %s FROM %s" name-table (util.device/build-str-select-params-for-joined) name-table)
              (format "INNER JOIN %s ON %s.id = %s.device_id" util.device/name-table util.device/name-table name-table)
              (when str-where (format "WHERE %s" str-where))]
             (join " "))
        items (jdbc/query (or transaction db-spec) query)]
    (map util.device/build-item-from-selected-params-joined items)))

(defn get-list-for-watch-scope [id-watch-scope & [{:keys [transaction]}]]
  (get-list-with-device
   {:str-where (format "watch_scope_id = %d" id-watch-scope)
    :transaction transaction}))

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-where str-where
    :transaction transaction}))

(defn get-list-with-total [params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction}))

(defn assign-to-list-watch-scope [list-watch-scope & [{:keys [transaction]}]]
  (let [str-list-ids-watch-scope
        (->> (for [item list-watch-scope] (str (:id item)))
             (join ",")
             (format "(%s)"))
        terms (get-list-with-device {:str-where (format "%s.watch_scope_id IN %s" name-table str-list-ids-watch-scope)
                                     :transaction transaction})]
    (for [watch-scope list-watch-scope]
      (assoc watch-scope :terms (filter #(= (:id watch-scope) (:watch_scope_id %)) terms)))))
