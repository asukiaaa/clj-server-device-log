(ns back.models.util.user-team-device-config
  (:require [back.models.util.core :as util.core]))

(def name-table "user_team_device_config")
(def key-table (keyword name-table))
(def keys-param [:id :config :user_team_id :device_id :created_at :updated_at])

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
