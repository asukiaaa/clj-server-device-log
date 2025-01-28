(ns front.model.device-file
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.device :as model.device]
            [front.model.util :as util]))

(def name-table "device_file")
(def keys-for-table [:path :device_id :created_at])
(defn build-str-keys-for-table [& [{:keys [without-device]}]]
  (join " "
        [(join " " (map name keys-for-table))
         (when-not without-device model.device/str-table-and-keys)]))

(defn fetch-list-and-total-for-device [{:keys [id-device on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device")
                              :str-keys-of-item (build-str-keys-for-table #_{:without-device true})
                              :str-params (format "device_id: %s" (util/build-input-str-for-int id-device))
                              :str-additional-field model.device/str-table-and-keys
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-latest-each-device [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_latest_each_device")
                              :str-keys-of-item (build-str-keys-for-table)
                              :on-receive on-receive
                              :limit limit
                              :page page}))
