(ns front.model.device-log
  (:require [goog.string :refer [format]]
            clojure.string
            [front.model.device :as model.device]
            [front.model.device-type :as model.device-type]
            [front.model.util :as util :refer [build-input-str-for-str]]))

(def name-table "device_log")
(def keys-for-device-logs [:id :device_id :device_name :created_at :data])
(def str-keys-for-device-logs (clojure.string/join " " (map name keys-for-device-logs)))
#_(def str-keys-for-device-logs-with-device-id (str str-keys-for-device " device{id name}"))

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
  (let [first-key (if (string? val-key) val-key (first val-key))
        target-field (when-not (empty? first-key)
                       (case first-key
                         "data" (or data (js->clj (.parse js/JSON (:data record))))
                         (get record (keyword first-key))))
        json-key (when-not (string? val-key) (rest val-key))]
    (get-by-json-key target-field json-key)))

(defn fetch-list-and-total [{:keys [str-where str-order limit page on-receive str-additional-field]} & [{:keys [str-name-table str-params]}]]
  (util/fetch-list-and-total {:name-table (or str-name-table (str name-table "s"))
                              :str-keys-of-item str-keys-for-device-logs
                              :str-additional-field str-additional-field
                              :str-params (str (if str-params (str str-params ", ") "")
                                               (format "where: %s, order: %s"
                                                       (build-input-str-for-str str-where) (build-input-str-for-str str-order)))
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-list-and-total-for-device [{:keys [id-device str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :str-additional-field (model.device/build-str-table-and-keys)
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_device")
                         :str-params (format "device_id: %d" id-device)}))

(defn fetch-list-and-total-for-device-type [{:keys [id-device-type str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :str-additional-field (model.device-type/build-str-table-and-keys)
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_device_type")
                         :str-params (format "device_type_id: %d" id-device-type)}))

(defn fetch-list-and-total-for-watch-scope [{:keys [id-watch-scope str-where str-order limit page on-receive]}]
  (fetch-list-and-total {:str-where str-where
                         :str-order str-order
                         :limit limit
                         :pate page
                         :on-receive on-receive}
                        {:str-name-table (str name-table "s_for_watch_scope")
                         :str-params (format "watch_scope_id: %d" id-watch-scope)}))
