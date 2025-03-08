(ns back.models.user-team-device-type-config
  (:refer-clojure :exclude [update])
  (:require [clojure.core :refer [format]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join]]
            [back.config :refer [db-spec]]
            [back.models.util.user-team-device-type-config :as util.user-team-device-type-config]
            [back.models.util :as model.util]
            [back.models.util.device-type :as util.device-type]
            [back.models.util.user-team :as util.user-team]))

(def name-table util.user-team-device-type-config/name-table)
(def key-table util.user-team-device-type-config/key-table)
(defn build-query-select-with-peripherals []
  (format "%s.*, %s, %s" name-table
          (util.device-type/build-str-select-params-for-joined)
          (util.user-team/build-str-select-params-for-joined)))
(defn build-query-join-tables []
  (join " " [(format "INNER JOIN %s ON %s.id = %s.device_type_id"
                     util.device-type/name-table
                     util.device-type/name-table
                     name-table)
             (format "INNER JOIN %s ON %s.id = %s.user_team_id"
                     util.user-team/name-table
                     util.user-team/name-table
                     name-table)]))
(defn build-item [item]
  (-> item
      util.device-type/build-item-from-selected-params-joined
      util.user-team/build-item-from-selected-params-joined))

(defn filter-params [params]
  (select-keys params [:user_team_id :device_type_id :config]))

(defn get-list-with-total-for-device-type [params id-device-type & [{:keys [transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table params
   {:str-keys-select (build-query-select-with-peripherals)
    :str-before-where (build-query-join-tables)
    :str-where (format "device_type_id = %d"
                       id-device-type)
    :build-item build-item
    :transaction transaction}))

(defn get-by-user-team-and-device-type [id-user-team id-device-tupe  & [{:keys [transaction]}]]
  (let [query (format "SELECT %s FROM %s %s WHERE user_team_id = %d AND device_type_id = %d"
                      (build-query-select-with-peripherals)
                      name-table
                      (build-query-join-tables)
                      id-user-team id-device-tupe)]
    (-> (jdbc/query (or transaction db-spec) query)
        first
        build-item)))

(defn create [params & [{:keys [transaction]}]]
  (model.util/create name-table (filter-params params) {:transaction transaction}))

(defn build-query-vals [params keys-of-params]
  (->> (for [key keys-of-params]
         (let [val (key params)]
           (cond
             (string? val)
             (model.util/build-input-str-for-str val)
             (number? val)
             (str val)
             :else
             "NULL")))
       (join ",")
       (format "(%s)")))

(defn build-query-keys [keys-of-params]
  (->> (for [key keys-of-params]
         (name key))
       (join ",")
       (format "(%s)")))

(defn update [id-user-team id-device-type params & [{:keys [transaction]}]]
  (let [params (-> (assoc params
                          :user_team_id id-user-team
                          :device_type_id id-device-type)
                   (filter-params))
        keys-of-params (keys params)
        query (format "INSERT INTO %s %s VALUES %s ON DUPLICATE KEY UPDATE config = %s"
                      name-table
                      (build-query-keys keys-of-params)
                      (build-query-vals params keys-of-params)
                      (model.util/build-input-str-for-str (:config params)))]
    (jdbc/query (or transaction db-spec) query)))

(defn delete [id-user-team id-device-type & [{:keys [transaction]}]]
  (jdbc/delete! (or transaction db-spec) key-table ["user_team_id = ? AND device_type_id = ?" id-user-team id-device-type]))
