(ns front.model.device-group
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [front.model.util :as util]))

(def keys-for-device-group [:id :user_id :name :created_at :updated_at])
(def str-keys-for-device-group (clojure.string/join " " (map name keys-for-device-group)))

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table "device_groups"
                              :str-keys-of-list str-keys-for-device-group
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table "device_group"
                     :str-keys-of-list str-keys-for-device-group
                     :id id
                     :on-receive on-receive}))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table "device_group"
                      :id id
                      :on-receive on-receive}))
