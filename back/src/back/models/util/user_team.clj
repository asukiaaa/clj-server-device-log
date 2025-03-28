(ns back.models.util.user-team
  (:require [back.models.util.core :as util.core]))

(def name-table "user_team")
(def key-table (keyword name-table))
(def keys-param [:id :name :owner_user_id :created_at :updated_at])

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params & [{:keys [name-table-destination]}]]
  (util.core/build-item-from-selected-params-joined name-table keys-param params {:name-table-destination name-table-destination}))
