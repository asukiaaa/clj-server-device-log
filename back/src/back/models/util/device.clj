(ns back.models.util.device
  (:require [back.models.util.core :as util.core]))

(def name-table "device")
(def key-table (keyword name-table))
(def keys-param [:id :name :created_at :updated_at :device_type_id :user_team_id])

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
