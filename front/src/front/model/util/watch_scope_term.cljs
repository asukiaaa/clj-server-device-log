(ns front.model.util.watch-scope-term
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]))

(def name-table "watch_scope_term")
(def key-table (keyword name-table))
(def keys-for-table [:id :watch_scope_id :device_id :datetime_from :datetime_until :created_at :updated_at])
(def str-keys-for-table (join " " (map name keys-for-table)))
(defn build-str-table-and-keys []
  (format "%s {%s}"
          name-table
          str-keys-for-table))

(defn term->param-str [term]
  (format "device_id: %s, datetime_from: %s, datetime_until: %s"
          (util/build-input-str-for-int (:device_id term))
          (util/build-input-str-for-str (:datetime_from term))
          (util/build-input-str-for-str (:datetime_until term))))

(defn term-list->param-str [terms]
  (->> (for [term terms]
         (format "{%s}" (term->param-str term)))
       (join ",")
       (format "[%s]")))
