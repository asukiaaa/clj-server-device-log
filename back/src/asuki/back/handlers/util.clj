(ns asuki.back.handlers.util
  (:require [clojure.string :as str]
            [asuki.back.config :refer [key-auth]]))

(defn match-bearer [req]
  (-> (:headers req)
      (get "authorization")
      (str/split #" ")
      #_((fn [x] (println x) x))
      (last)
      (= key-auth)))
