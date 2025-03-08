(ns front.model.util.device-type
  (:require [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "device_type")
(def key-table (keyword name-table))
(def keys-for-table [:id :user_id :name :config_format :config_default :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [params-optional]]
  (util.core/build-query-table-and-keys
   name-table query-keys params-optional))

(defn build-info-query-fetch-by-id [id on-receive]
  (util.core/build-info-query-fetch-by-id
   {:name-table name-table
    :query-keys-of-item query-keys
    :id id
    :on-receive on-receive}))
