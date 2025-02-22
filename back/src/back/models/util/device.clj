(ns back.models.util.device
  (:require [clojure.core :refer [format]]
            [clojure.string :refer [join]]))

(def name-table "device")
(def key-table (keyword name-table))
(def keys-param [:id :name :created_at :updated_at :device_type_id :user_team_id])
(def str-keys-param (map name keys-param))

(defn build-str-select-params-for-joined []
  (->> (for [str-key-param str-keys-param]
         (format  "%s.%s joined_%s_%s" name-table str-key-param name-table str-key-param))
       (join ",")))

(defn build-device-item-from-selected-params-joined [params]
  (let [device (into {} (for [key-param keys-param]
                          [key-param (get params (keyword (format "joined_%s_%s" name-table (name key-param))))]))]
    (assoc params key-table device)))
