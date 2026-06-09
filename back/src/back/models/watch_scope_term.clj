(ns back.models.watch-scope-term
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.models.util.device :as util.device]
            [back.models.util.device-log :as util.device-log]
            [back.models.util.device-file :as util.device-file]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.watch-scope :as util.watch-scope]
            [back.models.util.watch-scope-term :as util.watch-scope-term]))

(def name-table util.watch-scope-term/name-table)
(def key-table util.watch-scope-term/key-table)

(defn build-str-join-tables []
  (join " " [(format "INNER JOIN %s ON %s.id = %s.device_id"
                     util.device/name-table
                     util.device/name-table
                     name-table)
             (format "INNER JOIN %s ON %s.id = %s.watch_scope_id"
                     util.watch-scope/name-table
                     util.watch-scope/name-table
                     name-table)]))

(defn build-str-keys-select-with-peripherals []
  (format "%s.*, %s, %s"
          name-table
          (util.device/build-str-select-params-for-joined)
          (util.watch-scope/build-str-select-params-for-joined)))

(defn build-item [item]
  (-> item
      util.device/build-item-from-selected-params-joined
      util.watch-scope/build-item-from-selected-params-joined
      #_((fn [item] (println item) item))))

(defn filter-params [params]
  (select-keys params [:id :device_id :watch_scope_id :datetime_from :datetime_until]))

(defn- build-sql-ids-some-table-for-watch-scope [id-watch-scope name-table-other str-key-datetime-to-compare]
  (->> [(format "SELECT %s.id FROM %s INNER JOIN %s ON %s.device_id = %s.device_id"
                name-table-other
                name-table-other
                name-table
                name-table-other
                name-table)
        "WHERE"
        (format "%s.watch_scope_id = %d" name-table id-watch-scope)
        "AND"
        (util.watch-scope-term/build-sql-datetime-is-target str-key-datetime-to-compare)
        (format "GROUP BY %s.id" name-table-other)]
       (join " ")
       (format "(%s)")))

(defn build-sql-ids-device-log-for-watch-scope [id-watch-scope]
  (build-sql-ids-some-table-for-watch-scope
   id-watch-scope
   util.device-log/name-table
   (format "%s.created_at" util.device-log/name-table)))

(defn build-sql-ids-device-file-for-watch-scope [id-watch-scope]
  (build-sql-ids-some-table-for-watch-scope
   id-watch-scope
   util.device-file/name-table
   (format "%s.recorded_at" util.device-file/name-table)))

(defn get-by-id [id & [{:keys [transaction str-where]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (build-str-keys-select-with-peripherals)
    :str-before-where (build-str-join-tables)
    :str-where str-where
    :build-item build-item
    :transaction transaction}))

(defn get-by-id-for-user-teams [id sql-ids-user-team & [{:keys [transaction]}]]
  (get-by-id
   id
   {:str-where (format "%s.%s_id IN %s" util.device/name-table util.user-team/name-table sql-ids-user-team)
    :transaction transaction}))

(defn delete [id & [{:keys [transaction]}]]
  (model.util/delete key-table id {:transaction transaction}))

(defn update [id params & [{:keys [transaction]}]]
  (->> (model.util/update key-table id params {:transaction transaction})
       (assoc {} key-table)))

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
  (let [item (model.util/create
              key-table
              (filter-params params)
              {:transaction transaction})]
    {key-table item}))

(defn delete-list-for-watch-scope [id-watch-scope & [{:keys [transaction]}]]
  (println :delete-list-for-watch-scoep id-watch-scope)
  (jdbc/delete! (or transaction db-spec) key-table ["watch_scope_id = ?" id-watch-scope]))

(defn create-list-for-watch-scope [id-watch-scope terms & [{:keys [transaction]}]]
  (let [params (for [term terms] (-> term filter-params (assoc :watch_scope_id id-watch-scope)))]
    (jdbc/insert-multi! (or transaction db-spec) key-table params)))

(defn get-list-with-device [{:keys [str-where transaction]}]
  (let [query
        (->> [(format "SELECT %s.*, %s FROM %s" name-table (util.device/build-str-select-params-for-joined) name-table)
              (format "INNER JOIN %s ON %s.id = %s.device_id" util.device/name-table util.device/name-table name-table)
              (when str-where (format "WHERE %s" str-where))]
             (join " "))
        items (jdbc/query (or transaction db-spec) query)]
    (map util.device/build-item-from-selected-params-joined items)))

(defn get-list-with-watch-scope [{:keys [str-where transaction]}]
  #_(println :get-list-with-watch-scope str-where)
  (let [query
        (->> [(format "SELECT %s.*, %s FROM %s" name-table (util.watch-scope/build-str-select-params-for-joined) name-table)
              (format "INNER JOIN %s ON %s.id = %s.watch_scope_id" util.watch-scope/name-table util.watch-scope/name-table name-table)
              (when str-where (format "WHERE %s" str-where))]
             (join " "))
        items (jdbc/query (or transaction db-spec) query)]
    (map util.watch-scope/build-item-from-selected-params-joined items)))

(defn get-list-for-watch-scope [id-watch-scope & [{:keys [transaction]}]]
  (get-list-with-device
   {:str-where (format "watch_scope_id = %d" id-watch-scope)
    :transaction transaction}))

(defn- get-list-with-total-base [params & [{:keys [str-where transaction]}]]
    (model.util/get-list-with-total-with-building-query
 name-table params
 {:str-where str-where
  :str-keys-select (build-str-keys-select-with-peripherals)
  :str-before-where (build-str-join-tables)
  :build-item build-item
  :transaction transaction}))

(defn get-list-with-total [params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:transaction transaction}))

(defn get-list-with-total-for-device [id-device params & [{:keys [transaction]}]]
  (get-list-with-total-base params {:str-where (format "%s.device_id = %s" name-table id-device)
                                    :transaction transaction}))

(defn assign-to-list-watch-scope [list-watch-scope & [{:keys [transaction]}]]
  (when-not (empty? list-watch-scope)
    (let [str-list-ids-watch-scope
          (->> (for [item list-watch-scope] (str (:id item)))
               (join ",")
               (format "(%s)"))
          terms (get-list-with-device {:str-where (format "%s.watch_scope_id IN %s" name-table str-list-ids-watch-scope)
                                       :transaction transaction})]
      (for [watch-scope list-watch-scope]
        (assoc watch-scope util.watch-scope/key-terms (filter #(= (:id watch-scope) (:watch_scope_id %)) terms))))))

(defn assign-actives-to-list-device [list-device & [{:keys [transaction]}]]
  (when-not (empty? list-device)
    (let [str-list-ids-device
          (->> (for [item list-device] (str (:id item)))
               (join ",")
               (format "(%s)"))
          terms (get-list-with-watch-scope
                 {:str-where (format "%s.device_id IN %s AND %s"
                                     name-table str-list-ids-device (util.watch-scope-term/build-sql-is-active))
                  :transaction transaction})]
      (for [device list-device]
        (assoc device util.device/key-active-watch-scope-terms (filter #(= (:id device) (:device_id %)) terms))))))

(defn assign-actives-to-device [device & [{:keys [transaction]}]]
  (when device
    (first (assign-actives-to-list-device [device] {:transaction transaction}))))
