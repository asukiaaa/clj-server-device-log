(ns front.model.util
  (:require [clojure.string :refer [escape]]))

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
