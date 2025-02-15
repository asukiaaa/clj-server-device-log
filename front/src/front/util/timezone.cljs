(ns front.util.timezone
  (:refer-clojure :exclude [get set])
  (:require [cljs-time.format :as tf]
            ["date-fns" :refer [parse isValid format]]
            ["@date-fns/tz" :refer [TZDate]]
            [front.view.util.label :as util.label]))

(def format-datetime-with-zone (tf/formatter "YYYY-MM-dd HH:mm:ss.SSS Z"))
(def format-datetime (tf/formatter "YYYY-MM-dd HH:mm:ss.SSS"))

(defn get-from-browzer [] (-> (js/Intl.DateTimeFormat) .resolvedOptions .-timeZone))
(defn get-from-localstorage [] (.getItem js/localStorage "timezone"))
(defn set [timezone] (.setItem js/localStorage "timezone" timezone))
(defn get []
  (if-let [timezone-from-localstorage (get-from-localstorage)]
    timezone-from-localstorage
    (let [timezone (get-from-browzer)]
      (set timezone)
      timezone)))

(defn parse-datetime [str-datetime]
  (try
    (tf/parse format-datetime-with-zone str-datetime)
    (catch js/Error _
      (let [time (tf/parse format-datetime str-datetime)]
        #_(println time)
        time))))

(def date-fns-format-with-timezone "yyyy-MM-dd HH:mm:ss.SSS XX")
(def date-fns-format-with-timezone-until-second "yyyy-MM-dd HH:mm:ss XX")
(def date-fns-format-with-timezone-until-minutes "yyyy-MM-dd HH:mm XX")

(defn build-datetime-str-in-timezone [str-datetime & [{:keys [datetime-format str-timezone]}]]
  (if (empty? str-datetime)
    util.label/invalid-date
    (let [datetime-format (or datetime-format date-fns-format-with-timezone-until-second)
          date (parse str-datetime date-fns-format-with-timezone (TZDate.))
          date (when-not (isValid date)
                 (parse (str str-datetime " +0000") date-fns-format-with-timezone (TZDate.)))
          str-timezone (or str-timezone (get-from-browzer))]
      #_(.log js/console date)
      #_(println (isValid date) (format date date-fns-format-with-timezone))
      (if (isValid date)
        (format (.withTimeZone date str-timezone) datetime-format)
        util.label/invalid-date))))
