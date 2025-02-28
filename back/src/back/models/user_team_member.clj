(ns back.models.user-team-member
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util.user :as util.user]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.user-team-member :as util.user-team-member]
            [back.models.util :as model.util]))

(def name-table util.user-team-member/name-table)
(def key-table util.user-team-member/key-table)
(defn build-str-keys-select []
  (format "%s.*, %s, %s"
          name-table
          (util.user/build-str-select-params-for-joined)
          (util.user-team/build-str-select-params-for-joined)))
(defn build-str-before-where []
  (join " " [(format "INNER JOIN %s ON %s.id = %s.member_id"
                     util.user/name-table
                     util.user/name-table
                     name-table)
             (format "INNER JOIN %s ON %s.id = %s.user_team_id"
                     util.user-team/name-table
                     util.user-team/name-table
                     name-table)]))
(defn build-item [item]
  (-> item
      (util.user/build-item-from-selected-params-joined {:name-table-destination util.user-team-member/name-member-user})
      (util.user-team/build-item-from-selected-params-joined)))

(defn filter-params [params]
  (select-keys params [:user_team_id :member_id :permission]))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select)
    :str-before-where (build-str-before-where)
    :build-item build-item
    :transaction transaction}))

(defn delete [id & [{:keys [transaction]}]]
  (jdbc/delete! (or transaction db-spec) key-table ["id = ?" id]))

(defn update [id params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [t-con (or transaction db-spec)]
    (jdbc/update! db-spec key-table params ["id = ?" id])
    {key-table (get-by-id id {:transaction t-con})}))

; TODO limit user for updating owner_user_id
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
  {key-table (model.util/create key-table (filter-params params) {:transaction transaction})})

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table
   params
   {:str-keys-select (build-str-keys-select)
    :str-before-where (build-str-before-where)
    :build-item build-item
    :str-where str-where
    :transaction transaction}))

(defn get-list-with-total-for-user-team [params id-user-team & [{:keys [transaction]}]]
  (get-list-with-total-base params {:str-where (format "%s.user_team_id = %d" name-table id-user-team)
                                    :transaction transaction}))

(defn get-list-with-total [params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction}))

(defn build-query-id-user-team [id-user-team-member]
  (-> [(format "SELECT user_team_id from %s" name-table)
       (format "WHERE id = %d" id-user-team-member)]))
