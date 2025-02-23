(ns front.model.watch-scope
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [front.model.util.device :as util.device]
            [front.model.util.watch-scope :as util.watch-scope]
            [front.model.util.watch-scope-term :as util.term]
            [front.model.util.user-team :as util.user-team]
            [front.model.util :as util]))

(def name-table util.watch-scope/name-table)
(def key-table util.watch-scope/key-table)
(def query-keys util.watch-scope/query-keys)

(defn build-query-keys-with-watch-joined-tables []
  (format "%s %s %s {%s %s {%s}}"
          query-keys
          (util.user-team/build-query-table-and-keys)
          util.watch-scope/name-watch-scope-terms-on-field
          util.term/query-keys
          util.device/name-table
          util.device/query-keys))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)
          name (:name item)]
      [id (str id " " name)])))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s")
                              :str-keys-of-item (build-query-keys-with-watch-joined-tables)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table name-table
                     :str-keys-of-item (build-query-keys-with-watch-joined-tables)
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table (str name-table)
                      :id id
                      :on-receive on-receive}))

(defn create [{:keys [name user_team_id terms on-receive]}]
  (let [str-params (format "%s: {name: %s, %s: %s, user_team_id: %s}"
                           name-table
                           (util/build-input-str-for-str name)
                           util.watch-scope/name-watch-scope-terms-on-field
                           (util.term/term-list->param-str terms)
                           (util/build-input-str-for-int user_team_id))]
    (util/create {:name-table name-table
                  :str-keys-receive (util.watch-scope/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name terms on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, %s: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           util.watch-scope/name-watch-scope-terms-on-field
                           (util.term/term-list->param-str terms))]
    (util/update {:name-table name-table
                  :str-keys-receive (util.watch-scope/build-query-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
