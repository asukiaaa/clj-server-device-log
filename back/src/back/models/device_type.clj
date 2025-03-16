(ns back.models.device-type
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.models.util.device :as util.device]
            [back.models.util.device-type :as util.device-type]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.user-team-permission :as util.user-team-permission]))

(def name-table util.device-type/name-table)
(def key-table util.device-type/key-table)

(defn build-str-keys-select-with-peripherals []
  (format "%s.*, %s"
          name-table
          (util.user-team/build-str-select-params-for-joined)))
(defn build-str-join-tables []
  (format "INNER JOIN %s ON %s.id = %s.manager_user_team_id"
          util.user-team/name-table
          util.user-team/name-table
          name-table))
(defn build-item [item]
  (-> item
      (util.user-team/build-item-from-selected-params-joined
       {:name-table-destination util.device-type/name-manager-user-team})))

(defn- build-query-filter-by-user-teams-via-manager [sql-ids-user-team]
  (format "%s.%s_id IN %s"
          name-table
          util.device-type/name-manager-user-team
          sql-ids-user-team))

(defn- build-query-filter-by-user-teams-via-device [sql-ids-user-team]
  (let [query-select-types-for-user-team
        (format "(SELECT device_type_id FROM %s WHERE user_team_id IN %s GROUP BY device_type_id)"
                util.device/name-table
                sql-ids-user-team)]
    (format "%s.id IN %s"
            name-table query-select-types-for-user-team)))

(defn build-query-filter-by-user-teams-via [sql-ids-user-team & [{:keys [via-device via-manager]}]]
  (->> [(when via-device (build-query-filter-by-user-teams-via-device sql-ids-user-team))
        (when via-manager (build-query-filter-by-user-teams-via-manager sql-ids-user-team))]
       (remove nil?)
       (join " OR ")))

(defn get-by-id [id & [{:keys [str-where transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-tables)
    :str-where str-where
    :build-item build-item
    :transaction transaction}))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec key-table params ["id = ?" id]))

(defn update-for-user [{:keys [id id-user params]}]
  (jdbc/with-db-transaction [t-con db-spec]
    (jdbc/update!
     db-spec key-table params
     [(format "id = %d AND %s"
              id (build-query-filter-by-user-teams-via-manager
                  (util.user-team-permission/build-query-ids-for-user-write id-user)))])
    {key-table (get-by-id id {:transaction t-con})}))

(defn delete-for-user [{:keys [id id-user]}]
  (jdbc/delete!
   db-spec key-table
   [(format "id = %d AND %s"
            id (build-query-filter-by-user-teams-via-manager
                (util.user-team-permission/build-query-ids-for-user-write id-user)))]))

(defn get-by-id-for-user-teams-via [id sql-ids-user-team & [{:keys [transaction via-device via-manager]}]]
  (get-by-id
   id
   {:str-where (build-query-filter-by-user-teams-via
                sql-ids-user-team
                {:via-device via-device :via-manager via-manager})
    :transaction transaction}))

(defn get-by-id-for-user-via [id id-user & [{:keys [transaction via-device via-manager]}]]
  (get-by-id-for-user-teams-via
   id (util.user-team-permission/build-query-ids-for-user-show id-user)
   {:transaction transaction :via-device via-device :via-manager via-manager}))

(defn get-by-id-for-user-to-edit [id id-user & [{:keys [transaction]}]]
  (get-by-id-for-user-teams-via
   id (util.user-team-permission/build-query-ids-for-user-write id-user)
   {:transaction transaction :via-manager true}))

(defn create [params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [t-con (or transaction db-spec)]
    (jdbc/insert! t-con key-table (util.device-type/filter-params params))
    (let [id (-> (jdbc/query t-con "SELECT LAST_INSERT_ID()")
                 first vals first)
          item (get-by-id id {:transaction t-con})]
      {key-table item})))

(defn get-list-with-total [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-where str-where
    :str-before-where (build-str-join-tables)
    :build-item build-item
    :transaction transaction}))

(defn get-list-with-total-for-user-teams-via [params sql-ids-user-team & [{:keys [transaction via-device via-manager]}]]
  (get-list-with-total
   params
   {:str-where (build-query-filter-by-user-teams-via
                sql-ids-user-team
                {:via-device via-device :via-manager via-manager})
    :transaction transaction}))

(defn get-list-with-total-for-user-team-via [params id-user-team & [{:keys [transaction via-device via-manager]}]]
  (get-list-with-total-for-user-teams-via
   params
   (format "(%d)" id-user-team)
   {:transaction transaction
    :via-device via-device
    :via-manager via-manager}))

(defn get-list-with-total-for-user-via [params id-user & [{:keys [transaction via-device via-manager]}]]
  (get-list-with-total-for-user-teams-via
   params
   (util.user-team-permission/build-query-ids-for-user-show id-user)
   {:transaction transaction
    :via-device via-device
    :via-manager via-manager}))
