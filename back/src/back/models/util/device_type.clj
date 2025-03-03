(ns back.models.util.device-type
  (:require [back.models.util.core :as util.core]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def keys-param [:id :name :user_id :config_format :config_default :created_at :updated_at])

(def keys-param-input [:name :user_id :config_format :config_default])
(defn filter-params [params]
  (select-keys params keys-param-input))

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
