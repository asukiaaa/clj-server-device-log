(ns front.model.device-watch-group
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def name-table "device_watch_group")
(def keys-for-table [:id :owner_user_id :name :memo :created_at :updated_at])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item str-keys-for-table
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item str-keys-for-table
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table)
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name memo id-owner-user on-receive]}]
  (let [str-params (goog.string.format "%s: {name: %s, memo: %s, owner_user_id: %s}"
                                       name-table
                                       (util/build-input-str-for-str name)
                                       (util/build-input-str-for-str memo)
                                       (util/build-input-str-for-int id-owner-user))]
    (util/create {:name-table name-table
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name memo id-owner-user on-receive]}]
  (let [str-params (goog.string.format "id: %s, %s: {name: %s, memo: %s, owner_user_id: %s}"
                                       (util/build-input-str-for-int id)
                                       name-table
                                       (util/build-input-str-for-str name)
                                       (util/build-input-str-for-str memo)
                                       (util/build-input-str-for-int id-owner-user))]
    (util/update {:name-table name-table
                  :str-keys-receive (goog.string.format "%s { %s }"
                                                        name-table
                                                        str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
