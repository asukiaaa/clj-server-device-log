(ns front.model.util.device-type
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_id :name :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [query-keys-additional]}]]
  (format "%s {%s%s}"
          name-table
          query-keys
          (when query-keys-additional (str " " query-keys-additional))))
