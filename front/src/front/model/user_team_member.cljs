(ns front.model.user-team-member
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util :as util]
            [front.model.util.user :as util.user]
            [front.model.util.user-team :as util.user-team]
            [front.model.util.user-team-member :as util.user-team-member]))

(def name-table util.user-team-member/name-table)
(def query-keys util.user-team-member/query-keys)
(defn build-query-member-table-and-keys []
  (format "%s{%s}"
          util.user-team-member/name-member-user
          util.user/query-keys))
(defn build-query-keys-with-peripheral []
  (format "%s %s %s"
          query-keys
          (build-query-member-table-and-keys)
          (util.user-team/build-query-table-and-keys)))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total-for-user-team [{:keys [id-user-team on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (format "%ss_for_%s" name-table util.user-team/name-table)
                              :str-params (format "user_team_id: %s" (util/build-input-str-for-int id-user-team))
                              :str-keys-of-item (build-query-keys-with-peripheral)
                              :str-additional-field (util.user-team/build-query-table-and-keys)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id-for-user-team [{:keys [id user_team_id on-receive]}]
  (util/fetch-by-id {:name-table (format "%s_for_%s" name-table util.user-team/name-table)
                     :str-keys-of-item (build-query-keys-with-peripheral)
                     :id id
                     :str-additional-params
                     (format "user_team_id: %s"
                             (util/build-input-str-for-int user_team_id))
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table)
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [user_email user_team_id permission on-receive]}]
  (let [str-params (format "%s: {user_email: %s, permission: %s, user_team_id: %s}"
                           name-table
                           (util/build-input-str-for-str user_email)
                           (util/build-input-str-for-str permission)
                           (util/build-input-str-for-int user_team_id))]
    (util/create {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            query-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id permission on-receive]}]
  (let [str-params (format "id: %s, %s: {permission: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str permission))]
    (util/update {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            query-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
