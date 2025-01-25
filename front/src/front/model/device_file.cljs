(ns front.model.device-file
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.device :as model.device]
            [front.model.util :as util]))

(def name-table "device_file")
(def keys-for-table [:path :device_id])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))

(defn fetch-list-and-total-for-device [{:keys [id-device on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device")
                              :str-keys-of-item str-keys-for-table
                              :str-params (format "device_id: %s" (util/build-input-str-for-int id-device))
                              :str-additional-field model.device/str-table-and-keys
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-device-group [{:keys [id-device-group on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_latest_for_device_group")
                              :str-keys-of-item str-keys-for-table
                              :on-receive on-receive
                              :limit limit
                              :page page}))
