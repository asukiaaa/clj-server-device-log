(ns back.util.time
  (:require [clj-time.format :as time.format]))

(def timeformat-datetime-with-millis (time.format/formatter "yyyyMMdd-HHmmss-SSS"))
