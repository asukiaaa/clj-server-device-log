(ns front.model.util.session-permission
  (:require [clojure.string :refer [join]]
            [front.model.util.core :as util.core]))

(def name-table "session_permission")
(def key-table (keyword name-table))
(def keys-for-table [:ids_user_team_editable])
(def query-keys (join " " (map name keys-for-table)))

(defn build-query-table-and-keys [& [{:keys [query-keys-additional] :as params-optional}]]
  (util.core/build-query-table-and-keys
   name-table query-keys params-optional))
