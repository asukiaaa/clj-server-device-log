(ns back.models.util.watch-scope
  (:require [back.models.util.core :as util.core]))

(def name-table "watch_scope")
(def key-table (keyword name-table))
(def keys-param [:id :user_team_id :name :updated_at :created_at])
(def key-terms :terms)

(defn build-query-get-id-user-team [id]
  (->> (format "SELECT user_team_id FROM %s WHERE id = %d"
               name-table
               id)
       (format "(%s)")))

(defn build-str-select-params-for-joined []
  (util.core/build-str-select-params-for-joined name-table keys-param))

(defn build-item-from-selected-params-joined [params & [{:keys [name-table-destination]}]]
  (util.core/build-item-from-selected-params-joined
   name-table keys-param
   params {:name-table-destination name-table-destination}))
