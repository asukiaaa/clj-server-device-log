(ns front.model.util.user-team
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "user_team")
(def key-table (keyword name-table))
(def keys-for-table [:id :owner_user_id :name :memo :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys []
  (format "%s {%s}"
          name-table
          query-keys))
