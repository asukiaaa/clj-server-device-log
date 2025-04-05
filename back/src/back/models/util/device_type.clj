(ns back.models.util.device-type
  (:require [back.models.util.core :as util.core]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def keys-param [:id :name :manager_user_team_id :config_format :config_default :config_renderer_default :created_at :updated_at])
(def key-manager-user-team :manager_user_team)
(def name-manager-user-team (name key-manager-user-team))

(def keys-param-input [:name :manager_user_team_id :config_format :config_default :config_renderer_default])
(defn filter-params [params]
  (select-keys params keys-param-input))

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params]
  (util.core/build-item-from-selected-params-joined name-table keys-param params))
