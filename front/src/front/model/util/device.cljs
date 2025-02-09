(ns front.model.util.device
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]))

(def name-table "device")
(def keys-for-device [:id :device_type_id :user_team_id :name :created_at :updated_at])
(def str-keys-for-device (join " " (map name keys-for-device)))
