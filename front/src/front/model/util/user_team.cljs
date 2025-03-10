(ns front.model.util.user-team
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "user_team")
(def key-table (keyword name-table))
(def keys-for-table [:id :owner_user_id :name :memo :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [name-table]}]]
  (format "%s {%s}"
          (or name-table @#'name-table)
          query-keys))

(defn build-info-query-fetch-list-and-total [{:keys [name-table query-keys-of-item limit page query-params query-additional-field on-receive]}]
  (util.core/build-info-query-fetch-list-and-total
   {:name-table (or name-table (str @#'name-table "s"))
    :query-keys-of-item (or query-keys-of-item query-keys)
    :limit limit
    :page page
    :query-params query-params
    :query-additional-field query-additional-field
    :on-receive on-receive}))
