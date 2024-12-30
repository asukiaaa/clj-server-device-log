(ns asuki.back.handlers.util
  (:require [clojure.string :as str]))

(defn get-bearer [req]
  (-> (:headers req)
      (get "authorization")
      (or "")
      (str/split #" ")
      ((fn [arr]
         (when (= (first arr) "Bearer")
           (last arr))))))
