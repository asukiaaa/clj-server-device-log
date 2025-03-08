(ns front.model.device-type
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util :as util]
            [front.model.util.device-type :as util.device-type]
            [front.model.util.user-team :as util.user-team]))

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

(defn fetch-list-and-total-for-user-team [{:keys [on-receive user_team_id limit page]}]
  (util/fetch-list-and-total
   {:name-table (format "%ss_for_%s"
                        name-table util.user-team/name-table)
    :str-keys-of-item query-keys
    :str-params (format "user_team_id: %s"
                        (util/build-input-str-for-int user_team_id))
    :str-additional-field (util.user-team/build-query-table-and-keys)
    :on-receive on-receive
    :limit limit
    :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (-> (util.device-type/build-info-query-fetch-by-id id on-receive)
      (util/fetch-info-query)))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name config_default config_format on-receive]}]
  (let [str-params (format "%s: {name: %s, config_default: %s, config_format: %s}"
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str config_default)
                           (util/build-input-str-for-str config_format))]
    (util/create {:name-table name-table
                  :str-keys-receive (util.device-type/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name config_default config_format on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, config_default: %s, config_format: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           (util/build-input-str-for-str config_default)
                           (util/build-input-str-for-str config_format))]
    (util/update {:name-table name-table
                  :str-keys-receive (util.device-type/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
