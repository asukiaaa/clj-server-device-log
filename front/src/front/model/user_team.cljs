(ns front.model.user-team
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util.user-team :as util.user-team]
            [front.model.util :as util]
            [front.model.util.device-type :as util.device-type]))

(def name-table util.user-team/name-table)
(def key-table util.user-team/key-table)
(def query-keys util.user-team/query-keys)

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (-> {:limit limit :page page :on-receive on-receive}
      util.user-team/build-info-query-fetch-list-and-total
      util/fetch-info-query))

(defn fetch-list-and-total-for-device-type [{:keys [device_type_id on-receive limit page]}]
  (-> {:limit limit :page page :on-receive on-receive
       :name-table (str name-table "s_for_device_type")
       :query-additional-field (util.device-type/build-query-table-and-keys)
       :query-params (format "device_type_id: %d" device_type_id)}
      util.user-team/build-info-query-fetch-list-and-total
      util/fetch-info-query))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item query-keys
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table)
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name memo owner_user_id on-receive]}]
  (let [str-params (format "%s: {name: %s, memo: %s, owner_user_id: %s}"
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str memo)
                           (util/build-input-str-for-int owner_user_id))]
    (util/create {:name-table name-table
                  :str-keys-receive (util.user-team/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name memo owner_user_id on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, memo: %s, owner_user_id: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str memo)
                           (util/build-input-str-for-int owner_user_id))]
    (util/update {:name-table name-table
                  :str-keys-receive (util.user-team/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
