(ns front.model.util.device
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "device")
(def keys-for-table [:id :device_type_id :user_team_id :name :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [query-keys-additional] :as params-optional}]]
  (util.core/build-query-table-and-keys
   name-table query-keys params-optional))
