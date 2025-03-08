(ns front.model.user-team-device-type-config
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util.device-type :as util.device-type]
            [front.model.util.user-team :as util.user-team]
            [front.model.util.user-team-device-type-config :as util.user-team-device-type-config]
            [front.model.util :as util]))

(def name-table util.user-team-device-type-config/name-table)
(def key-table util.user-team-device-type-config/key-table)

(defn build-query-keys-with-peripherals []
  (format "%s %s %s"
          util.user-team-device-type-config/query-keys
          (util.device-type/build-query-table-and-keys)
          (util.user-team/build-query-table-and-keys)))
(defn build-str-table-and-keys []
  (format "%s { %s }" name-table util.user-team-device-type-config/query-keys))

(defn fetch-list-and-total-for-device-type [{:keys [device_type_id on-receive limit page]}]
  (util/fetch-list-and-total
   {:name-table (str name-table "s_for_device_type")
    :str-keys-of-item (build-query-keys-with-peripherals)
    :on-receive on-receive
    :str-additional-field (util.device-type/build-query-table-and-keys)
    :str-params (format "device_type_id: %s" (util/build-input-str-for-int device_type_id))
    :limit limit
    :page page}))

#_(defn fetch-by-id [{:keys [id on-receive]}]
    (util/fetch-by-id {:name-table name-table
                       :str-keys-of-item query-keys
                       :id id
                       :on-receive on-receive}))

(defn fetch-by-user-team-and-device-type [{:keys [device_type_id user_team_id on-receive to-edit]}]
  (util/fetch {:name-table (if to-edit (str name-table "_to_edit") name-table)
               :str-keys-of-item (build-query-keys-with-peripherals)
               :str-params (format "user_team_id: %s, device_type_id: %s"
                                   (util/build-input-str-for-int user_team_id)
                                   (util/build-input-str-for-int device_type_id))
               :on-receive on-receive}))

(defn delete [{:keys [user_team_id device_type_id on-receive]}]
  (util/delete {:name-table name-table
                :str-input-params (format "user_team_id: %s, device_type_id: %s"
                                          (util/build-input-str-for-int user_team_id)
                                          (util/build-input-str-for-int device_type_id))
                :on-receive on-receive}))

(defn update [{:keys [user_team_id device_type_id config on-receive]}]
  (let [str-params (format "user_team_id: %s, device_type_id: %s, %s: {config: %s}"
                           (util/build-input-str-for-int user_team_id)
                           (util/build-input-str-for-int device_type_id)
                           name-table
                           (util/build-input-str-for-str config))]
    (util/update {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (format "Delete %s for %s and %s"
          name-table
          (-> item util.user-team/key-table :name)
          (-> item util.device-type/key-table :name)))
