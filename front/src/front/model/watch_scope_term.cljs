(ns front.model.watch-scope-term
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util.watch-scope-term :as util.watch-scope-term]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.util :as util]))

(def name-table util.watch-scope-term/name-table)
(def str-keys-for-table util.watch-scope-term/str-keys-for-table)

(defn fetch-list-and-total-for-watch-scope [{:keys [id-watch-scope on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_watch_scope")
                              :str-keys-of-item str-keys-for-table
                              :str-additional-field (model.watch-scope/build-str-table-and-keys)
                              :on-receive on-receive
                              :str-params (format "watch_scope_id: %s" (util/build-input-str-for-int id-watch-scope))
                              :limit limit
                              :page page}))

#_(defn fetch-by-id [{:keys [id on-receive]}]
    (util/fetch-by-id {:name-table name-table
                       :str-keys-of-item str-keys-for-table
                       :id id
                       :on-receive on-receive}))

(defn fetch-by-id-for-watch-scope [{:keys [id id-watch-scope on-receive]}]
  (util/fetch-by-id {:name-table (str name-table "_for_watch_scope")
                     :str-keys-of-item str-keys-for-table
                     :id id
                     :str-additional-params (format "watch_scope_id: %s" (util/build-input-str-for-int id-watch-scope))
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table name-table
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [display-name id-device id-watch-scope on-receive]}]
  (let [str-params (format "%s: {display_name: %s, device_id: %s, watch_scope_id: %s}"
                           name-table
                           (util/build-input-str-for-str display-name)
                           (util/build-input-str-for-int id-device)
                           (util/build-input-str-for-int id-watch-scope))]
    (util/create {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id display-name on-receive]}]
  (let [str-params (format "id: %s, %s: {display_name: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str display-name))]
    (util/update {:name-table name-table
                  :str-keys-receive (format "%s { %s }"
                                            name-table
                                            str-keys-for-table)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
