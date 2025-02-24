(ns front.model.device-file
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util.device :as util.device]
            [front.model.util.watch-scope :as util.watch-scope]
            [front.model.util :as util]))

(def name-table "device_file")
(def keys-for-table [:path :path_thumbnail :device_id :recorded_at :created_at])
(def query-keys (join " " (map name keys-for-table)))
(defn build-query-keys-with-device []
  (format "%s %s %s{%s}"
          query-keys
          (util.device/build-query-table-and-keys)
          (str util.watch-scope/name-table "s")
          util.watch-scope/query-keys))

(defn fetch-list-and-total-for-watch-scope [{:keys [id-watch-scope on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_watch_scope")
                              :str-keys-of-item (build-query-keys-with-device)
                              :str-params (format "watch_scope_id: %s" (util/build-input-str-for-int id-watch-scope))
                              :str-additional-field (util.watch-scope/build-query-table-and-keys)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-device [{:keys [id-device on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_for_device")
                              :str-keys-of-item (build-query-keys-with-device)
                              :str-params (format "device_id: %s" (util/build-input-str-for-int id-device))
                              :str-additional-field (util.device/build-query-table-and-keys)
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-latest-each-device [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table (str name-table "s_latest_each_device")
                              :str-keys-of-item (build-query-keys-with-device)
                              :on-receive on-receive
                              :limit limit
                              :page page}))
