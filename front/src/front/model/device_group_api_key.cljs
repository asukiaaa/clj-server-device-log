(ns front.model.device-group-api-key
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util :as util]))

(def name-table "device_group_api_key")
(def keys-for-table [:id :device_group_id :name :key_str :permission :created_at :updated_at])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))

(defn fetch-list-and-total-for-device-group [{:keys [id-device-group on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device_group")
                              :str-keys-of-item str-keys-for-table
                              :on-receive on-receive
                              :str-params (format "device_group_id: %s" (util/build-input-str-for-int id-device-group))
                              :limit limit
                              :page page}))

#_(defn fetch-by-id [{:keys [id on-receive]}]
    (util/fetch-by-id {:name-table name-table
                       :str-keys-of-item str-keys-for-table
                       :id id
                       :on-receive on-receive}))

(defn fetch-by-id-for-device-group [{:keys [id-device-group-api-key id-device-group on-receive]}]
  (util/fetch-by-id {:name-table (str name-table "_for_device_group")
                     :str-keys-of-item str-keys-for-table
                     :id id-device-group-api-key
                     :str-additional-params (format "device_group_id: %s" (util/build-input-str-for-int id-device-group))
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table "_for_user")
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name permission id-device-group on-receive]}]
  (let [str-params (format "%s: {name: %s, permission: %s, device_group_id: %s}"
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str permission)
                           (util/build-input-str-for-int id-device-group))]
    (util/create {:name-table (str name-table "_for_user")
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name permission on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, permission: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str permission))]
    (util/update {:name-table (str name-table "_for_user")
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device-group]
  (str "delete " name-table " id:" (:id device-group) " name:" (:name device-group)))

(defn build-key-post [device-group-api-key]
  (str "device_group:" (:device_group_id device-group-api-key) ":device_group_api_key:" (:id device-group-api-key) ":" (:key_str device-group-api-key)))
