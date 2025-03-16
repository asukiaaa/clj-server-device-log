(ns back.models.util.user-permission
  (:require [clojure.string :refer [join]]
            [clojure.core :refer [format]]
            [back.models.user :as util.user]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.user-team-member :as util.user-team-member]))

(defn build-query-ids-for-user-teams [sql-ids-user-team]
  (->> [(format "SELECT %s.id FROM %s"
                util.user/name-table
                util.user/name-table)
        (format "LEFT JOIN %s ON %s.owner_user_id = %s.id"
                util.user-team/name-table
                util.user-team/name-table
                util.user/name-table)
        (format "LEFT JOIN %s ON %s.member_id = %s.id"
                util.user-team-member/name-table
                util.user-team-member/name-table
                util.user/name-table)
        "WHERE"
        (->> [(format "%s.id IN %s"
                      util.user-team/name-table
                      sql-ids-user-team)
              (format "%s.user_team_id IN %s"
                      util.user-team-member/name-table
                      sql-ids-user-team)]
             (join " OR "))
        (format "GROUP BY %s.id" util.user/name-table)]
       (join " ")
       (format "(%s)")))
