(ns back.models.watch-scope
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.watch-scope :as util.watch-scope]))

(def name-table util.watch-scope/name-table)
(def key-table util.watch-scope/key-table)
(defn build-str-keys-select-with-join []
  (format "%s.*, %s" name-table (util.user-team/build-str-select-params-for-joined)))
(defn build-str-join []
  (format "LEFT JOIN %s ON %s.id = %s.user_team_id"
          util.user-team/name-table
          util.user-team/name-table
          name-table))

(defn build-item [item]
  (util.user-team/build-item-from-selected-params-joined item))

(defn filter-params [params]
  (select-keys params [:name :user_team_id]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select-with-join)
    :str-before-where (build-str-join)
    :build-item build-item
    :transaction transaction}))

(defn get-by-id-for-user-teams [id sql-ids-user-team & [{:keys [transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select-with-join)
    :str-before-where (build-str-join)
    :str-where (format "%s.id IN %s" util.user-team/name-table sql-ids-user-team)
    :build-item build-item
    :transaction transaction}))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params & [{:keys [transaction]}]]
  (println :update-watch-scope id (filter-params params))
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! (or transaction db-spec) key-table (filter-params params) ["id = ?" id])
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

(defn create [params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [t-con (or transaction db-spec)]
    (jdbc/insert! t-con key-table (filter-params params))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      item)))

(defn get-list-with-total [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-before-where (build-str-join)
    :str-keys-select (build-str-keys-select-with-join)
    :build-item build-item
    :str-where str-where
    :transaction transaction}))

(defn get-list-with-total-for-user-team [params id-user-team & [{:keys [transaction]}]]
  (get-list-with-total params {:str-where (format "user_team_id = %d" id-user-team) :transaction transaction}))

(defn get-list-with-total-for-user-teams [params sql-ids-user-team & [{:keys [transaction]}]]
  (get-list-with-total params {:str-where (format "user_team_id IN %s" sql-ids-user-team)} {:transaction transaction}))
