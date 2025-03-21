(ns front.model.device-type-api-key
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util.device-type :as util.device-type]
            [front.model.util.authorization-bearer :as bearer]
            [front.model.util :as util]))

(def name-table "device_type_api_key")
(def keys-for-table [:id :device_type_id :name :permission :created_at :updated_at])
(def query-keys (clojure.string/join " " (map name keys-for-table)))
(defn build-str-table-and-keys []
  (format "%s { %s }" name-table query-keys))

(defn fetch-list-and-total-for-device-type [{:keys [id-device-type on-receive limit page]}]
  (util/fetch-list-and-total
   {:name-table (str name-table "s_for_device_type")
    :str-keys-of-item query-keys
    :on-receive on-receive
    :str-additional-field (util.device-type/build-query-table-and-keys)
    :str-params (format "device_type_id: %s" (util/build-input-str-for-int id-device-type))
    :limit limit
    :page page}))

#_(defn fetch-by-id [{:keys [id on-receive]}]
    (util/fetch-by-id {:name-table name-table
                       :str-keys-of-item query-keys
                       :id id
                       :on-receive on-receive}))

(defn fetch-by-id-for-device-type [{:keys [id-device-type-api-key id-device-type on-receive]}]
  (util/fetch-by-id {:name-table (str name-table "_for_device_type")
                     :str-keys-of-item query-keys
                     :id id-device-type-api-key
                     :str-additional-params (format "device_type_id: %s" (util/build-input-str-for-int id-device-type))
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

(defn create [{:keys [name permission id-device-type on-receive]}]
  (let [str-params (format "%s: {name: %s, permission: %s, device_type_id: %s}"
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str permission)
                           (util/build-input-str-for-int id-device-type))]
    (util/create {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name permission on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, permission: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str permission))]
    (util/update {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
