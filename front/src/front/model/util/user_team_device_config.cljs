(ns front.model.util.user-team-device-config
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]))

(def name-table "user_team_device_config")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_team_id :device_id :config :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys []
  (format "%s {%s}"
          name-table
          query-keys))

(defn build-query-params-for-device [{:keys [config user_team_id]}]
  (format "config: %s, user_team_id: %s"
          (util/build-input-str-for-str config)
          (util/build-input-str-for-int user_team_id)))
