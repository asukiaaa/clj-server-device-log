(ns front.model.device-group
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def name-table "device_group")
(def keys-for-device-group [:id :user_id :name :created_at :updated_at])
(def str-keys-for-device-group (clojure.string/join " " (map name keys-for-device-group)))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table "device_groups"
                              :str-keys-of-item str-keys-for-device-group
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item str-keys-for-device-group
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table "_for_user")
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name on-receive]}]
  (let [str-params (goog.string.format "device_group: {name: %s}"
                                       (util/build-input-str-for-str name))]
    (util/create {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-device-group)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name on-receive]}]
  (let [str-params (goog.string.format "id: %s, device_group: {name: %s}"
                                       (util/build-input-str-for-int id)
                                       (util/build-input-str-for-str name))]
    (util/update {:name-table (str name-table "_for_user")
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-device-group)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [device-group]
  (str "delete device_group id:" (:id device-group) " name:" (:name device-group)))
