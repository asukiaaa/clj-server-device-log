(ns front.model.util.watch-scope
  (:require [clojure.string :refer [join]]
            [goog.string :refer [format]]))

(def name-table "watch_scope")
(def key-table (keyword name-table))
(def name-watch-scope-terms-on-field "terms")
(def keys-for-table [:id :user_team_id :name :created_at :updated_at])
(def query-keys (join " " (map name keys-for-table)))
(defn build-query-table-and-keys []
  (format "%s{%s}" name-table query-keys))
