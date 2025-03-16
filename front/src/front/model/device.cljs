(ns front.model.device
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]
            [front.model.util.authorization-bearer :as bearer]
            [front.model.util.device :as util.device]
            [front.model.util.device-type :as util.device-type]
            [front.model.util.user-team :as util.user-team]
            [front.model.util.user-team-device-config :as util.user-team-device-config]
            [front.model.util.watch-scope :as util.watch-scope]
            [front.model.util.watch-scope-term :as util.watch-scope-term]))

(def name-table util.device/name-table)
(defn build-query-keys-of-periperals []
  (join " " [(util.device-type/build-query-table-and-keys)
             (util.user-team/build-query-table-and-keys)
             (util.user-team-device-config/build-query-table-and-keys)
             (util.watch-scope-term/build-query-table-and-keys
              {:name-table (name util.device/key-active-watch-scope-terms)
               :query-additional-keys (util.watch-scope/build-query-table-and-keys)})]))
(defn build-query-keys-with-peripherals []
  (join " " [util.device/query-keys
             (build-query-keys-of-periperals)]))
(defn build-query-table-and-keys-with-peripherals []
  (util.device/build-query-table-and-keys
   {:query-keys-additional (build-query-keys-of-periperals)}))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          value (str (-> item :device_type :name) " "(:name item) )]
      [id value])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item (build-query-keys-with-peripherals)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-user-team [{:keys [on-receive user_team_id limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_user_team")
                              :str-params (format "user_team_id: %s" (util/build-input-str-for-int user_team_id))
                              :str-keys-of-item (build-query-keys-with-peripherals)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item (build-query-keys-with-peripherals)
                     :id id
                     :on-receive on-receive}))

(defn fetch-authorization-bearer-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table (format "authorization_bearer_for_%s" name-table)
                     :str-keys-of-item bearer/str-keys
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn- build-query-user-team-device-config [id-user-team str-config]
  (if (empty? (str str-config))
    ""
    (->> (util.user-team-device-config/build-query-params-for-device
          {:config str-config
           :user_team_id id-user-team})
         (format "%s:{%s}" util.user-team-device-config/name-table))))

(defn create [{:keys [name device_type_id user_team_id user_team_device_config_config on-receive]}]
  (let [str-params (format "%s: {device_type_id: %s, user_team_id: %s name: %s} %s"
                           name-table
                           (util/build-input-str-for-int device_type_id)
                           (util/build-input-str-for-int user_team_id)
                           (util/build-input-str-for-str name)
                           (build-query-user-team-device-config user_team_id user_team_device_config_config))]
    (util/create {:name-table name-table
                  :str-keys-receive (util.device/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name device_type_id user_team_id user_team_device_config_config on-receive]}]
  (let [str-params (format "id: %s, %s: {device_type_id: %s, user_team_id: %s, name: %s} %s"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-int device_type_id)
                           (util/build-input-str-for-int user_team_id)
                           (util/build-input-str-for-str name)
                           (build-query-user-team-device-config user_team_id user_team_device_config_config))]
    (util/update {:name-table name-table
                  :str-keys-receive (util.device/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device]
  (str "delete " name-table " id:" (:id device) " name:" (:name device)))
