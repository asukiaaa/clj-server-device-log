(ns front.model.util.user-team
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]))

(def name-table "user_team")
(def key-table (keyword name-table))
(def keys-for-table [:id :owner_user_id :name :memo :created_at :updated_at])
(def str-keys-for-table (clojure.string/join " " (map name keys-for-table)))

(defn build-str-table-and-keys []
  (format "%s {%s}"
          name-table
          str-keys-for-table))
