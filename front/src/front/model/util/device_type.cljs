(ns front.model.util.device-type
  (:require [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def key-config-renderer-default :config_renderer_default)
(def keys-for-table [:id :name :manager_user_team_id :config_format :config_default key-config-renderer-default :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))
(def key-manager-user-team :manager_user_team)
(def name-manager-user-team (name key-manager-user-team))

(defn build-query-table-and-keys [& [params-optional]]
  (util.core/build-query-table-and-keys
   name-table query-keys params-optional))

(defn build-info-query-fetch-by-id [id on-receive & [{:keys [query-keys]}]]
  (util.core/build-info-query-fetch-by-id
   {:name-table name-table
    :query-keys-of-item (or query-keys @#'name-table)
    :id id
    :on-receive on-receive}))
