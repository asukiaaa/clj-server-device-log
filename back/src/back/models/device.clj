(ns back.models.device
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.util.encryption :as encryption]
            [back.models.util.device :as util.device]
            [back.models.util.device-type :as util.device-type]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.user-team-device-config :as util.user-team-device-config]
            [back.models.util :as model.util]
            [back.models.util.user-team-permission :as util.user-team-permission]
            [back.models.device-type :as model.device-type]))

(def name-table util.device/name-table)
(def key-table util.device/key-table)

(defn build-str-join-tables []
  (join " " [(format "INNER JOIN %s ON %s.id = %s.device_type_id"
                     util.device-type/name-table
                     util.device-type/name-table
                     name-table)
             (format "LEFT JOIN %s ON %s.id = %s.user_team_id"
                     util.user-team/name-table
                     util.user-team/name-table
                     name-table)
             (format "LEFT JOIN %s ON %s.user_team_id = %s.user_team_id AND %s.device_id = %s.id"
                     util.user-team-device-config/name-table
                     util.user-team-device-config/name-table
                     name-table
                     util.user-team-device-config/name-table
                     name-table)]))

(defn build-str-keys-select-with-peripherals []
  (format "%s.*, %s, %s, %s"
          name-table
          (util.device-type/build-str-select-params-for-joined)
          (util.user-team/build-str-select-params-for-joined)
          (util.user-team-device-config/build-str-select-params-for-joined)))

(defn build-item [item]
  (-> item
      util.device-type/build-item-from-selected-params-joined
      util.user-team/build-item-from-selected-params-joined
      util.user-team-device-config/build-item-from-selected-params-joined
      #_((fn [item] (println item) item))))

(defn filter-params [params]
  (select-keys params [:name :device_type_id :user_team_id]))

(defn get-by-id [id & [{:keys [str-where transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-tables)
    :str-where str-where
    :build-item build-item
    :transaction transaction}))

(defn build-authorization-bearer-for-item [item]
  (model.util/build-authorization-bearer (:key_str item) key-table :key_str))

(defn get-authorizaton-bearer-by-id [id-device & [{:keys [transaction]}]]
  (let [item (get-by-id id-device {:transaction transaction})]
    (build-authorization-bearer-for-item item)))

(defn get-list-by-ids [sql-ids & [{:keys [transaction]}]]
  (let [query (format "SELECT * from %s WHERE id IN %s"
                      name-table
                      sql-ids)]
    (jdbc/query (or transaction db-spec) query)))

(defn get-by-key-str [key-str & [{:keys [transaction]}]]
  (when-not (empty? key-str)
    (let [query (format "SELECT * from %s WHERE key_str = \"%s\"" name-table (model.util/escape-for-sql key-str))
          user (first (jdbc/query (or transaction db-spec) [query]))]
      user)))

(defn get-by-authorization-bearer [bearer & [{:keys [transaction]}]]
  (let [decoded-bearer (encryption/decode bearer)
        key-str (-> decoded-bearer key-table :key_str)]
    (get-by-key-str key-str {:transaction transaction})))

(defn delete [id]
  (jdbc/delete! db-spec key-table ["id = ?" id]))

(defn update-for-admin [id params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (jdbc/update! (or transaction db-spec) key-table params ["id = ?" id])
    (get-by-id id {:transaction transaction})))

(defn get-by-id-in-ids-user-team-or-ids-device [& [{:keys [id ids-user-team ids-device transaction]}]]
  (let [str-where (->> [(when ids-user-team
                          (format "(%s.user_team_id IN %s OR %s.manager_user_team_id IN %s)"
                                  name-table
                                  ids-user-team
                                  util.device-type/name-table
                                  ids-user-team))
                        (when ids-device
                          (format "%s.id IN %s" name-table ids-device))]
                       (remove nil?)
                       ((fn [arr] (if (seq arr)
                                    arr
                                    (throw (Exception. "ids-user-team or ids-device is needed")))))
                       (join " OR ")
                       (format "(%s)"))]
    (get-by-id
     id
     {:str-where str-where
      :transaction transaction})))

(defn get-by-id-for-user-to-edit [id id-user & [{:keys [transaction]}]]
  (let [sql-ids-user-team (util.user-team-permission/build-query-ids-for-user-write id-user)
        str-where (format "%s.manager_user_team_id IN %s"
                          util.device-type/name-table
                          sql-ids-user-team)]
    (get-by-id
     id
     {:str-where str-where
      :transaction transaction})))

(defn update-for-user [{:keys [id id-user params transaction]}]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (let [item (get-by-id-for-user-to-edit id id-user {:transaction transaction})]
      (if (empty? item)
        {:errors ["item not found"]}
        (do
          (jdbc/update! db-spec key-table params ["id = ?" id])
          (get-by-id id {:transaction transaction}))))))

(defn delete-for-user [id id-user]
  (jdbc/with-db-transaction [transaction db-spec]
    (let [item (get-by-id-for-user-to-edit id id-user {:transaction transaction})]
      (if (empty? item)
        {:errors ["device does not avairable"]}
        (do
          (jdbc/delete! db-spec key-table ["id = ?" id])
          {})))))

(defn build-key-str []
  (model.util/build-random-str-alphabets-and-number 60))

(defn- assign-unique-key-str-to-params [params transaction]
  (let [hash (build-key-str)
        user (get-by-key-str hash {:transaction transaction})]
    (if (empty? user)
      (assoc params :key_str hash)
      (assign-unique-key-str-to-params params transaction))))

(defn is-unique-name-for-device-type [params & [{:keys [transaction]}]]
  (let [name (model.util/build-input-str-for-str (:name params))
        id-device-type (:device_type_id params)
        query (join " " [(format "SELECT * FROM %s" name-table)
                         (format "WHERE name = %s AND device_type_id = %d"
                                 name id-device-type)])
        device (-> (jdbc/query (or transaction db-spec) query)
                   first)]
    (not device)))

(defn create [params & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (if (is-unique-name-for-device-type params {:transaction transaction})
      (do (jdbc/insert! transaction key-table (-> params filter-params (assign-unique-key-str-to-params transaction)))
          (let [id (-> (jdbc/query transaction "SELECT LAST_INSERT_ID()")
                       first vals first)
                item (get-by-id id {:transaction transaction})]
            item))
      {:errors {:name ["required to unique for device type"]}})))

(defn create-for-user [params user-id & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (let [device-type-id (:device_type_id params)
          ; TODO resolve the spagetti
          device-type (model.device-type/get-by-id-for-user-to-edit
                       device-type-id user-id {:via-manager true :transaction transaction})]
      (if (empty? device-type)
        {:errors ["device type does not avairable"]}
        (do
          (jdbc/insert! transaction key-table (-> params
                                                  filter-params
                                                  (assign-unique-key-str-to-params transaction)))
          (let [id (-> (jdbc/query transaction "SELECT LAST_INSERT_ID()")
                       first vals first)]
            (get-by-id id {:transaction transaction})))))))

(defn- build-query-filter-by-user-teams-via [sql-ids-user-team {:keys [via-device via-manager]}]
  (->> [(when via-device
          (format "%s.user_team_id IN %s"
                  name-table
                  sql-ids-user-team))
        (when via-manager
          (str (format "%s.manager_user_team_id IN %s"
                       util.device-type/name-table
                       sql-ids-user-team)))]
       (remove nil?)
       (join " OR ")
       (format "(%s)")))

(defn get-list-with-total-for-admin [params & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-tables)
    :str-order "name DESC"
    :str-where str-where
    :build-item build-item
    :transaction transaction}))

(defn get-list-with-total-for-user-teams-via [params sql-ids-user-team & [{:keys [via-device via-manager transaction]}]]
  (get-list-with-total-for-admin
   params
   {:str-where (build-query-filter-by-user-teams-via sql-ids-user-team {:via-device via-device :via-manager via-manager})
    :transaction transaction}))

(defn get-list-with-total-for-user-team-via [params id-user-team & [{:keys [via-device via-manager transaction]}]]
  (get-list-with-total-for-user-teams-via params (format "(%d)" id-user-team)
                                          {:via-device via-device
                                           :via-manager via-manager
                                           :transaction transaction}))

(defn get-config [id-device & [{:keys [transaction]}]]
  (let [query (join " " [(format "SELECT %s.config_default FROM %s" util.device-type/name-table name-table)
                         (format "INNER JOIN %s ON %s.device_type_id = %s.id"
                                 util.device-type/name-table
                                 name-table
                                 util.device-type/name-table)
                         (format "WHERE %s.id = %d" name-table id-device)])
        item (first (jdbc/query (or transaction db-spec) query))
        config-default (when-let [str-config (:config_default item)] (json/read-str str-config))]
    config-default))
