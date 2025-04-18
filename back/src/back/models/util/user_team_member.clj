(ns back.models.util.user-team-member
  (:require [back.models.util.core :as util.core]))

(def name-table "user_team_member")
(def key-table (keyword name-table))
(def keys-param [:id :permission :user_team_id :member_user_id :created_at :updated_at])
(def key-member-user :member)
(def name-member-user (name key-member-user))

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
