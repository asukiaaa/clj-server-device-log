(ns front.model.util
  (:require [clojure.string :refer [escape]]))

(defn escape-str [text]
  (when-not (nil? text)
    (escape text {\" "\\\""
                  \\ "\\\\"})))
