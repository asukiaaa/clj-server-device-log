(ns back.handlers.util
  (:require [clojure.string :as str]
            [back.util.encryption :as encryption]
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
  (encryption/decode user-encoded-in-session))

(defn decode-and-find-user-in-session [user-encoded-in-session]
  (when-let [user (decode-user-in-session user-encoded-in-session)]
    (model.user/get-by-id-with-permission (:id user))))

(defn encode-user-for-session [user]
  (encryption/encode user))
