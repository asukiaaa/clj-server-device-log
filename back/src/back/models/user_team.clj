(ns back.models.user-team
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util.user-team :as util.user-team]
            [back.models.util :as model.util]
            [back.models.util.user-team-member :as util.user-team-member]))

(def name-table util.user-team/name-table)
(def key-table util.user-team/key-table)

(defn filter-params [params]
  (select-keys params [:name :owner_user_id :memo]))

(defn build-sql-ids-for-user [id-user]
  (format "(SELECT id FROM %s WHERE owner_user_id = %s)"
          name-table
          id-user))

(defn get-by-id [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! db-spec key-table params ["id = ?" id])
    {key-table (get-by-id id {:transaction t-con})}))

; TODO limit user for updating owner_user_id
(defn update-for-owner-user [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update! db-spec key-table params ["id = ? AND owner_user_id = ?" id id-user])
    {key-table (get-by-id id {:transaction t-con})}))

(defn delete-for-owner-user [{:keys [id id-user]}]
  (jdbc/delete! db-spec key-table ["id = ? AND owner_user_id = ?" id id-user]))

(defn build-query-owner-or-member [id-user]
  (format "(%s.owner_user_id = %d OR %s.member_id = %d)"
          name-table id-user
          util.user-team-member/name-table id-user))

(defn build-query-owner-or-member-writable [id-user]
  (format "(%s.owner_user_id = %d OR (%s.member_id = %d AND JSON_VALUE(permission,\"$.admin\")))"
          name-table id-user
          util.user-team-member/name-table id-user))

(defn build-query-ids-all []
  (format "SELECT id FROM %s" name-table))

(defn- build-query-ids-for-user-base [id-user str-where]
  (->> [(build-query-ids-all)
        (format "LEFT JOIN %s ON %s.user_team_id = %s.id"
                util.user-team-member/name-table
                util.user-team-member/name-table
                name-table)
        (format "WHERE %s" str-where)]
       (join " ")
       (format "(%s)")))

(defn build-query-ids-for-user-show [id-user]
  (build-query-ids-for-user-base id-user (build-query-owner-or-member id-user)))

(defn build-query-ids-for-user-write [id-user]
  (build-query-ids-for-user-base id-user (build-query-owner-or-member id-user)))

(defn get-by-id-for-user [id id-user & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     (join " " [(format "SELECT %s.* FROM %s" name-table name-table)
                                (format "LEFT JOIN %s ON %s.user_team_id = %s.id"
                                        util.user-team-member/name-table
                                        util.user-team-member/name-table
                                        name-table)
                                (format "WHERE %s.id = %d AND %s"
                                        name-table id
                                        (build-query-owner-or-member id-user))]))))

(defn create [params]
  {key-table (model.util/create key-table (filter-params params))})

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query name-table params {:str-where str-where :transaction transaction}))

(defn get-list-with-total-for-owner-user [params user-id]
  (get-list-with-total-base params {:str-where (format "owner_user_id = %d" user-id)}))

(defn get-list-with-total [params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction}))

(defn- user-has-permission-base [query-id-user-team query-where & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     (join " " [(format "SELECT %s.id FROM %s" name-table name-table)
                                (format "LEFT JOIN %s ON %s.user_team_id = %s.id"
                                        util.user-team-member/name-table
                                        util.user-team-member/name-table
                                        name-table)
                                (format "WHERE %s.id = %s AND %s"
                                        name-table (if (number? query-id-user-team) (str query-id-user-team) query-id-user-team)
                                        query-where)]))))

(defn user-has-permission-to-write [{:keys [id-user-team id-user transaction]}]
  (user-has-permission-base id-user-team (build-query-owner-or-member-writable id-user) {:transaction transaction}))

(defn user-has-permission-to-read [{:keys [id-user-team id-user transaction]}]
  (user-has-permission-base id-user-team (build-query-owner-or-member id-user) {:transaction transaction}))
