(ns back.models.util.user-team-permission
  (:require [clojure.string :refer [join]]
            [back.models.util.user-team :as util.user-team]
            [back.models.util.user-team-member :as util.user-team-member]))

(defn build-query-owner-or-member [id-user]
  (format "(%s.owner_user_id = %d OR %s.member_id = %d)"
          util.user-team/name-table id-user
          util.user-team-member/name-table id-user))

(defn build-query-owner-or-member-writable [id-user]
  (format "(%s.owner_user_id = %d OR (%s.member_id = %d AND JSON_VALUE(permission,\"$.admin\")))"
          util.user-team/name-table id-user
          util.user-team-member/name-table id-user))

(defn- build-query-ids-for-user-base [str-where]
  (->> [(format "SELECT %s.id FROM %s"
                util.user-team/name-table
                util.user-team/name-table)
        (format "LEFT JOIN %s ON %s.user_team_id = %s.id"
                util.user-team-member/name-table
                util.user-team-member/name-table
                util.user-team/name-table)
        (format "WHERE %s" str-where)]
       (join " ")
       (format "(%s)")))

(defn build-query-ids-for-user-show [id-user]
  (build-query-ids-for-user-base (build-query-owner-or-member id-user)))

(defn build-query-ids-for-user-write [id-user]
  (build-query-ids-for-user-base (build-query-owner-or-member-writable id-user)))
