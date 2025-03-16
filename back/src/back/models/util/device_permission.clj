(ns back.models.util.device-permission
  (:require [clojure.string :refer [join]]
            [clojure.core :refer [format]]
            [back.models.util.device :as util.device]
            [back.models.util.device-type :as util.device-type]))

(defn build-query-ids-for-user-teams-via [sql-ids-user-team & [{:keys [via-device via-manager]}]]
  (->> [(format "SELECT %s.id FROM %s"
                util.device/name-table
                util.device/name-table)
        (format "INNER JOIN %s ON %s.id = %s.device_type_id"
                util.device-type/name-table
                util.device-type/name-table
                util.device/name-table)
        "WHERE"
        (->> [(when via-device
                (format "%s.user_team_id IN %s "
                        util.device/name-table
                        sql-ids-user-team))
              (when via-manager
                (format "%s.manager_user_team_id IN %s"
                        util.device-type/name-table
                        sql-ids-user-team))]
             (remove nil?)
             (join " OR "))]
       (join " ")
       (format "(%s)")))
