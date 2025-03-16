(ns back.models.util.device
  (:require [clojure.string :refer [join]]
            [back.models.util.core :as util.core]))

(def name-table "device")
(def key-table (keyword name-table))
(def keys-param [:id :name :created_at :updated_at :device_type_id :user_team_id])
(def key-active-watch-scope-terms :active_watch_scope_terms)

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))

(defn build-sql-ids []
  (format "(SELECT id FROM %s)"
          name-table))

(defn build-sql-ids-for-user-teams [sql-id-user-team]
  (format "(SELECT id FROM %s WHERE user_team_id IN %s)"
          name-table
          sql-id-user-team))

(defn build-sql-ids-user-team-for-device-type [id-device-type & [{:keys [sql-ids-user-team]}]]
  (format "(SELECT user_team_id from %s WHERE %s)"
          name-table
          (->> [(format "device_type_id = %d" id-device-type)
                (when sql-ids-user-team (format "user_team_id IN %s" sql-ids-user-team))]
               (remove nil?)
               (join " AND "))))
