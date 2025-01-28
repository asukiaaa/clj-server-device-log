(ns front.model.device
  (:refer-clojure :exclude [update])
  (:require goog.string
            [clojure.string :refer [join]]
            [front.model.device-type :as model.device-type]
            [front.model.util :as util]))

(def name-table "device")
(def keys-for-device [:id :device_type_id :user_team_id :name :hash_post :created_at :updated_at])
(def str-keys-for-device (join " " (map name keys-for-device)))
(defn build-str-keys-for-device-with-peripherals []
  (join " "
        [str-keys-for-device
         (model.device-type/build-str-table-and-keys)
         "user_team{id name owner_user_id}"]))
(defn build-str-table-and-keys []
  (goog.string.format "%s {%s}"
                      name-table
                      (build-str-keys-for-device-with-peripherals)
                      #_str-keys-for-device))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item (build-str-keys-for-device-with-peripherals)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item (build-str-keys-for-device-with-peripherals)
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name device_type_id user_team_id on-receive]}]
  (let [str-params (goog.string.format "%s: {device_type_id: %s, user_team_id: %s name: %s}"
                                       name-table
                                       (util/build-input-str-for-int device_type_id)
                                       (util/build-input-str-for-int user_team_id)
                                       (util/build-input-str-for-str name))]
    (util/create {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name device_type_id user_team_id on-receive]}]
  (let [str-params (goog.string.format "id: %s, %s: {device_type_id: %s, user_team_id: %s, name: %s}"
                                       (util/build-input-str-for-int id)
                                       name-table
                                       (util/build-input-str-for-int device_type_id)
                                       (util/build-input-str-for-int user_team_id)
                                       (util/build-input-str-for-str name))]
    (util/update {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device]
  (str "delete " name-table " id:" (:id device) " name:" (:name device)))
