(ns front.model.device-watch-group-device
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util :as util]))

(def name-table "device_watch_group_device")
(def keys-for-table [:id :device_watch_group_id :name_device :device_id :created_at :updated_at])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))

(defn fetch-list-and-total-for-device-watch-group [{:keys [id-device-watch-group on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device_watch_group")
                              :str-keys-of-item str-keys-for-table
                              :on-receive on-receive
                              :str-params (format "device_watch_group_id: %s" (util/build-input-str-for-int id-device-watch-group))
                              :limit limit
                              :page page}))

#_(defn fetch-by-id [{:keys [id on-receive]}]
    (util/fetch-by-id {:name-table name-table
                       :str-keys-of-item str-keys-for-table
                       :id id
                       :on-receive on-receive}))

(defn fetch-by-id-for-device-watch-group [{:keys [id id-device-watch-group on-receive]}]
  (util/fetch-by-id {:name-table (str name-table "_for_device_watch_group")
                     :str-keys-of-item str-keys-for-table
                     :id id
                     :str-additional-params (format "device_watch_group_id: %s" (util/build-input-str-for-int id-device-watch-group))
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name-device id-device id-device-watch-group on-receive]}]
  (let [str-params (format "%s: {name_device: %s, device_id: %s, device_watch_group_id: %s}"
                           name-table
                           (util/build-input-str-for-str name-device)
                           (util/build-input-str-for-int id-device)
                           (util/build-input-str-for-int id-device-watch-group))]
    (util/create {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name-device on-receive]}]
  (let [str-params (format "id: %s, %s: {name_device: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name-device))]
    (util/update {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
