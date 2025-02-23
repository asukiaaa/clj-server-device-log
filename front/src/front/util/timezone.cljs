(ns front.util.timezone
  (:refer-clojure :exclude [get set])
  (:require [cljs-time.format :as tf]
            ["date-fns" :as df]
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

(def date-fns-format-until-date "yyyy-MM-dd")
(def date-fns-format-until-minute (str date-fns-format-until-date " HH:mm"))
(def date-fns-format-until-second (str date-fns-format-until-minute ":ss"))
(def date-fns-format (str date-fns-format-until-second ".SSS"))
(def date-fns-format-with-timezone-until-minute (str date-fns-format-until-minute " XX"))
(def date-fns-format-with-timezone-until-second (str date-fns-format-until-second " XX"))
(def date-fns-format-with-timezone (str date-fns-format " XX"))
(def timezone-utc "UTC")
(defn build-options-for-select []
  (for [item [timezone-utc
              (get)]]
    [item item]))

(defn datetime-str-without-timzone->datetime-in-timezone [str-datetime & [{:keys [str-timezone]}]]
  (when-not (empty? str-datetime)
    (let [str-timezone (or str-timezone (get-from-browzer))
          date (reduce (fn [result date-format]
                         (if (df/isValid result)
                           result
                           (df/parse str-datetime date-format (.tz TZDate str-timezone))))
                       nil [date-fns-format-until-date
                            date-fns-format-until-minute
                            date-fns-format-until-second
                            date-fns-format])]
      date)))

(defn datetime->str-in-timezone [date & [{:keys [datetime-format str-timezone]}]]
  (let [str-timezone (or str-timezone (get-from-browzer))
        datetime-format (or datetime-format date-fns-format-with-timezone-until-second)]
    (if (df/isValid date)
      (df/format (.withTimeZone (TZDate. date) str-timezone) datetime-format)
      util.label/invalid-date)))

(defn build-datetime-str-in-timezone [str-datetime & [{:keys [datetime-format str-timezone]}]]
  (if (empty? str-datetime)
    util.label/invalid-date
    (let [date (df/parse str-datetime date-fns-format-with-timezone (TZDate.))
          date (when-not (df/isValid date)
                 (df/parse (str str-datetime " +0000") date-fns-format-with-timezone (TZDate.)))]
      (datetime->str-in-timezone date {:datetime-format datetime-format :str-timezone str-timezone}))))
