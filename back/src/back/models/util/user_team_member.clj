(ns back.models.util.user-team-member
  (:require [back.models.util.core :as util.core]))

(def name-table "user_team_member")
(def key-table (keyword name-table))
(def keys-param [:id :permission :user_team_id :member_id :created_at :updated_at])
(def str-keys-param (map name keys-param))
(def key-member-user :member)
(def name-member-user (name key-member-user))

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  #_(assoc params key-table {:id 1 :owner_user_id 2})
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
