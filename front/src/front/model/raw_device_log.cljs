(ns front.model.raw-device-log
  (:require goog.string
            [front.model.util :refer [escape-str]]
            [re-graph.core :as re-graph]))

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

(defn fetch-list [{:keys [str-where str-order limit offset on-receive]}]
  (let [query (goog.string.format "{ raw_device_logs(where: \"%s\", order: \"%s\", limit: %d, offset: %d) { total list { id created_at data } } }"
                                  (escape-str str-where) (escape-str str-order) (or limit 100) (or offset 0))]
    (re-graph/query query {} (fn [{:keys [data errors]}]
                               (let [received-total (-> data :raw_device_logs :total)
                                     received-logs  (-> data :raw_device_logs :list)]
                                 (on-receive received-logs received-total errors))))))

