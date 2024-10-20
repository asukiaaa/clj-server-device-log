(ns front.model.device
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def name-table "device")
(def keys-for-device [:id :device_group_id :name :hash_post :created_at :updated_at])
(def str-keys-for-device (clojure.string/join " " (map name keys-for-device)))
(def str-keys-for-device-with-device-group (str str-keys-for-device " device_group{id name user_id}"))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item str-keys-for-device-with-device-group
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item str-keys-for-device-with-device-group
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table "_for_user")
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name device_group_id on-receive]}]
  (let [str-params (goog.string.format "%s: {device_group_id: %s, name: %s}"
                                       name-table
                                       (util/build-input-str-for-int device_group_id)
                                       (util/build-input-str-for-str name))]
    (util/create {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s {%s}"
                                                        name-table
                                                        str-keys-for-device)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name device_group_id on-receive]}]
  (let [str-params (goog.string.format "id: %s, %s: {device_group_id: %s, name: %s}"
                                       (util/build-input-str-for-int id)
                                       name-table
                                       (util/build-input-str-for-int device_group_id)
                                       (util/build-input-str-for-str name))]
    (util/update {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s {%s}"
                                                        name-table
                                                        str-keys-for-device)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device]
  (str "delete " name-table " id:" (:id device) " name:" (:name device)))

(defn build-key-post [device]
  (str "device:" (:id device) ":" (:hash_post device)))
