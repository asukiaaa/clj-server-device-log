(ns front.model.device-type
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]
            [front.model.util.device-type :as util.device-type]))

(def name-table util.device-type/name-table)
(def key-table util.device-type/key-table)
(def query-keys util.device-type/query-keys)

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item query-keys
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item query-keys
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
                  :str-keys-receive (util.device-type/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name on-receive]}]
  (let [str-params (goog.string.format "id: %s, %s: {name: %s}"
                                       (util/build-input-str-for-int id)
                                       name-table
                                       (util/build-input-str-for-str name))]
    (util/update {:name-table name-table
                  :str-keys-receive (util.device-type/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
