(ns back.models.util.core
  (:require [clojure.core :refer [format]]
            [clojure.string :refer [join]]))

(defn build-str-select-params-for-joined [name-table keys-param]
  (->> (for [key-param keys-param]
         (let [str-key-param (name key-param)]
           (format  "%s.%s joined_%s_%s" name-table str-key-param name-table str-key-param)))
       (join ",")))

(defn build-item-from-selected-params-joined [name-table keys-param params & [{:keys [name-table-destination]}]]
  (let [item (->> (for [key-param keys-param]
                    (when-let [value (get params (keyword (format "joined_%s_%s" name-table (name key-param))))]
                      [key-param value]))
                  (remove nil?)
                  (into {}))]
    (if (empty? item)
      params
      (assoc params (keyword (or name-table-destination name-table)) item))))
