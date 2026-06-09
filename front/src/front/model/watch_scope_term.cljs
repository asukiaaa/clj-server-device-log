(ns front.model.watch-scope-term
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util.device :as util.device]
            [front.model.util.watch-scope :as util.watch-scope]
            [front.model.util.watch-scope-term :as util.watch-scope-term]
            [front.model.util :as util]
            [front.view.util.label :as util.label]))

(def name-table util.watch-scope-term/name-table)
(def query-keys util.watch-scope-term/query-keys)
(defn build-query-keys-with-joined-tables []
  (format "%s %s %s"
          query-keys
          (util.watch-scope/build-query-table-and-keys)
          (util.device/build-query-table-and-keys)))

(defn fetch-list-and-total-for-device [{:keys [id-device on-receive limit page order]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device")
                              :str-keys-of-item (build-query-keys-with-joined-tables)
                              :str-params (format "device_id: %s" (util/build-input-str-for-int id-device))
                              :str-additional-field (util.device/build-query-table-and-keys)
                              :order order
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-watch-scope [{:keys [id-watch-scope on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_watch_scope")
                              :str-keys-of-item query-keys
                              :str-additional-field (util.watch-scope/build-query-table-and-keys)
                              :on-receive on-receive
                              :str-params (format "watch_scope_id: %s" (util/build-input-str-for-int id-watch-scope))
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item (build-query-keys-with-joined-tables)
                     :id id
                     :on-receive on-receive}))

(defn fetch-by-id-for-watch-scope [{:keys [id id-watch-scope on-receive]}]
  (util/fetch-by-id {:name-table (str name-table "_for_watch_scope")
                     :str-keys-of-item query-keys
                     :id id
                     :str-additional-params (format "watch_scope_id: %s" (util/build-input-str-for-int id-watch-scope))
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [device_id watch_scope_id datetime_from datetime_until on-receive]}]
  (let [str-params (format "%s: {device_id: %s, watch_scope_id: %s, datetime_from: %s, datetime_until: %s}"
                           name-table
                           (util/build-input-str-for-int device_id)
                           (util/build-input-str-for-int watch_scope_id)
                           (util/build-input-str-for-str datetime_from)
                           (util/build-input-str-for-str datetime_until))]
    (util/create {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            query-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id on-receive datetime_from datetime_until]}]
  (let [str-params (format "id: %s, %s: {datetime_from: %s, datetime_until: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str datetime_from)
                           (util/build-input-str-for-str datetime_until))]
    (util/update {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            query-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (join " " [(util.label/delete)
             (util.label/term-of-watch-scope)
             (-> item util.device/key-table :name)
             (-> item util.watch-scope/key-table :name)]))
