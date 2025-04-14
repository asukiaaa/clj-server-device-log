(ns front.model.util.device-log
  (:require [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "device_log")
(def key-table (keyword name-table))
(def keys-for-table [:id :device_id :created_at :data])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [query-keys-additional] :as params-optional}]]
  (util.core/build-query-table-and-keys
   name-table query-keys params-optional))
