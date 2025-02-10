(ns front.model.watch-scope
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util.device :as util.device]
            [front.model.util.watch-scope-term :as util.term]
            [front.model.util :as util]))

(def name-table "watch_scope")
(def key-table (keyword name-table))
(def name-watch-scope-terms-on-field "terms")
(def keys-for-table [:id :user_team_id :name :created_at :updated_at])
(def str-keys-for-table (join " " (map name keys-for-table)))
(defn build-str-keys-for-table-with-watch-term-and-device []
  (format "%s %s {%s %s {%s}}"
          str-keys-for-table
          name-watch-scope-terms-on-field
          util.term/str-keys-for-table
          util.device/name-table
          util.device/str-keys-for-device))
(defn build-str-table-and-keys []
  (format "%s {%s}"
          name-table
          str-keys-for-table))
(defn build-str-table-and-keys-with-terms []
  (format "%s {%s}"
          name-table
          (build-str-keys-for-table-with-watch-term-and-device)))

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
                     :str-keys-of-item (build-str-keys-for-table-with-watch-term-and-device)
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
                           name-watch-scope-terms-on-field
                           (util.term/term-list->param-str terms)
                           (util/build-input-str-for-int user_team_id))]
    (util/create {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn update [{:keys [id name terms on-receive]}]
  (let [str-params (format "id: %s, %s: {name: %s, %s: %s}"
                           (util/build-input-str-for-int id)
                           name-table
                           (util/build-input-str-for-str name)
                           name-watch-scope-terms-on-field
                           (util.term/term-list->param-str terms))]
    (util/update {:name-table name-table
                  :str-keys-receive (build-str-table-and-keys)
                  :str-input-params str-params
                  :on-receive on-receive})))

(defn build-confirmation-message-for-deleting [item]
  (str "delete " name-table " id:" (:id item) " name:" (:name item)))
