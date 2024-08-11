(ns front.model.util
  (:require [clojure.string :refer [escape]]
            [goog.string :refer [format]]))

(defn build-str-args-offset-limit-for-index [limit page]
  #_(println :build-args :limit limit (int? limit) :page page (int? page))
  (let [limit (if (int? limit) limit (js/parseInt limit))
        page (if (int? page) page (js/parseInt page))]
    (->> [(when (int? limit) (format "limit: %d" limit))
          (when  (int? page) (format "page: %d" page))]
         (filter  seq)
         (clojure.string/join  ", "))))

(defn escape-int [val]
  (when-not (nil? val)
    (if (int? val)
      val
      (js/parseInt val))))

(defn escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))

(defn build-error-messages [errors]
  (for [e errors] (:message e)))
