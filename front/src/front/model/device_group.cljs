(ns front.model.device-group
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def name-table "device_group")
(def keys-for-device-group [:id :user_id :name :created_at :updated_at])
(def str-keys-for-device-group (clojure.string/join " " (map name keys-for-device-group)))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table "device_groups"
                              :str-keys-of-list str-keys-for-device-group
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-list str-keys-for-device-group
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table "_for_user")
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name on-receive]}]
  (let [str-params (goog.string.format "device_group: {name: \"%s\"}"
                                       (util/escape-str name))]
    (util/create {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-device-group)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name on-receive]}]
  (let [str-params (goog.string.format "id: %d, device_group: {name: \"%s\"}"
                                       (util/escape-int id)
                                       (util/escape-str name))]
    (util/update {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-device-group)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device-group]
  (str "delete device_groupp id:" (:id device-group) " name:" (:name device-group)))
