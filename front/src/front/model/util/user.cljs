(ns front.model.util.user
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "user")
(def key-table (keyword name-table))
(def keys-for-table-with-permission [:id :email :name :permission :created_at :updated_at])
(def query-keys-with-permission (join " " (map name keys-for-table-with-permission)))

(def keys-for-table (remove #(= % :permission) keys-for-table-with-permission))
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys []
  (format "%s {%s}"
          name-table
          query-keys))
