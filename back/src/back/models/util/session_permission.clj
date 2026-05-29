(ns back.models.util.session-permission
  (:require [clojure.string :refer [join]]
            [back.models.util.core :as util.core]))

(def name-table "session_permission")
(def key-table (keyword name-table))
(def keys-param [:ids_user_team_editable])
