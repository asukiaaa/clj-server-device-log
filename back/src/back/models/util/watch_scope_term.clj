(ns back.models.util.watch-scope-term
  (:require [clojure.core :refer [format]]))

(def name-table "watch_scope_term")
(def key-table (keyword name-table))

(defn build-sql-datetime-is-target [str-key-datetime-to-compare]
  (format "(((%s.datetime_from IS NULL) OR (%s.datetime_from < %s)) AND ((%s.datetime_until IS NULL) OR (%s.datetime_until > %s)))"
          name-table
          name-table
          str-key-datetime-to-compare
          name-table
          name-table
          str-key-datetime-to-compare))
