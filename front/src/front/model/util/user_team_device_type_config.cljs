(ns front.model.util.user-team-device-type-config
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "user_team_device_type_config")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_team_id :device_type_id :config :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys []
  (format "%s {%s}"
          name-table
          query-keys))
