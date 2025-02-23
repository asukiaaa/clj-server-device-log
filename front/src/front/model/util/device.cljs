(ns front.model.util.device
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "device")
(def keys-for-table [:id :device_type_id :user_team_id :name :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [query-keys-additional]}]]
  (format "%s {%s%s}"
          name-table
          query-keys
          (if query-keys-additional (str " " query-keys-additional) "")))
