(ns back.handlers.util
  (:require [clojure.string :as str]
            [buddy.sign.jwt :as jwt]
            [back.config :as config]
            [back.models.user :as model.user]))

(defn get-bearer [req]
  (-> (:headers req)
      (get "authorization")
      (or "")
      (str/split #" ")
      ((fn [arr]
         (when (= (first arr) "Bearer")
           (last arr))))))

(defn decode-user-in-session [user-encoded-in-session]
  (try
    (jwt/unsign user-encoded-in-session config/secret-for-session)
    (catch Exception e (println "catched" (.getMessage e)))))

(defn decode-and-find-user-in-session [user-encoded-in-session]
  (when-let [user (decode-user-in-session user-encoded-in-session)]
    (model.user/get-by-id (:id user))))

(defn encode-user-for-session [user]
  (jwt/sign user config/secret-for-session))
