(ns front.model.raw-device-log
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.util :as util :refer [build-input-str-for-str]]))

(def name-table "raw_device_log")
(def keys-for-raw-device-logs [:id :device_id :device_name :created_at :data])
(def str-keys-for-raw-device-logs (clojure.string/join " " (map name keys-for-raw-device-logs)))
#_(def str-keys-for-raw-device-logs-with-device-id (str str-keys-for-device " device{id name}"))

(defn get-by-json-key [data json-key]
  (when-not (nil? data)
    (cond
      (string? json-key) (get data json-key)
      (or (vector? json-key) (seq? json-key))
      (let [key (first json-key)
            new-data (get data key)
            new-json-key (rest json-key)]
        (if (= 1 (count json-key))
          new-data
          (get-by-json-key new-data new-json-key)))
      :else data)))

(defn get-label-from-col-config [col]
  (or (get col "label")
      (let [key (get col "key")]
        (if (string? key) key (last key)))))

(defn get-val-from-record [record val-key & [{:keys [data]}]]
  (let [data (or data (js->clj (.parse js/JSON (:data record))))
        first-key (if (string? val-key) val-key (first val-key))
        target-field (when-not (empty? first-key)
                       (case first-key
                         "data" data
                         "created_at" (:created_at record)
                         "id" (:id record)
                         :else nil))
        json-key (when-not (string? val-key) (rest val-key))]
    (get-by-json-key target-field json-key)))

(defn fetch-list-and-total [{:keys [str-where str-order limit page on-receive]} & {:keys [str-name-table str-params]}]
  (util/fetch-list-and-total {:name-table (or str-name-table (str name-table "s"))
                              :str-keys-of-item str-keys-for-raw-device-logs
                              :str-params (str (if str-params (str str-params ", ") "")
                                               (format "where: %s, order: %s"
                                                       (build-input-str-for-str str-where) (build-input-str-for-str str-order)))
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-device [{:keys [id-device str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_device")
                         :str-params (format "device_id: %d" id-device)}))

(defn fetch-list-and-total-for-device-group [{:keys [id-device-group str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_device_group")
                         :str-params (format "device_group_id: %d" id-device-group)}))

(defn fetch-list-and-total-for-device-watch-group [{:keys [id-device-watch-group str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_device_watch_group")
                         :str-params (format "device_watch_group_id: %d" id-device-watch-group)}))
