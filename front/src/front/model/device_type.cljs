(ns front.model.device-type
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_id :name :created_at :updated_at])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))
(defn build-str-table-and-keys []
  (goog.string.format "%s { %s }"
                      name-table
                      str-keys-for-table))

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
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name on-receive]}]
  (let [str-params (goog.string.format "%s: {name: %s}"
                                       name-table
                                       (util/build-input-str-for-str name))]
    (util/create {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name on-receive]}]
  (let [str-params (goog.string.format "id: %s, %s: {name: %s}"
                                       (util/build-input-str-for-int id)
                                       name-table
                                       (util/build-input-str-for-str name))]
    (util/update {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
