(ns back.util.encryption
  (:require [buddy.sign.jwt :as jwt]
            [back.config :as config]))

(defn decode [item]
  (try
    (jwt/unsign item config/secret-for-session)
    (catch Exception e (println "catched" (.getMessage e)))))

(defn encode [item]
  (jwt/sign item config/secret-for-session))
