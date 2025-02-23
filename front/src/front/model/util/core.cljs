(ns front.model.util.core
  (:require [goog.string :refer [format]]))

(defn build-query-table-and-keys [name-table query-keys & [{:keys [query-keys-additional]}]]
  (format "%s {%s%s}"
          name-table
          query-keys
          (if query-keys-additional (str " " query-keys-additional) "")))
