(ns front.model.util.watch-scope-term
  (:require [goog.string :refer [format]]
            [clojure.string :refer [join]]
            [front.model.util :as util]))

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
