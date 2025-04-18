(ns front.model.util.user-team-member
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "user_team_member")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_team_id :member_user_id :permission :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))
(def key-member-user :member)
(def name-member-user (name key-member-user))

(defn build-query-table-and-keys []
  (format "%s {%s}"
          name-table
          query-keys))
