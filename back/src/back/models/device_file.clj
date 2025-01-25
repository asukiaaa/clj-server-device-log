(ns back.models.device-file
  (:refer-clojure :exclude [update])
  (:require [clojure.java.io :as io]
            [clojure.core :refer [re-pattern re-seq]]
            [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format :as time.format]
            [back.config :refer [path-filestorage]]
            [back.models.device :as model.device]))

(def timeformat-datetime-with-millis (time.format/formatter "yyyyMMdd-HHmmss-SSS"))
(def path-url-filestorage "/filestorage")

(defn build-dir-for-device-after-filestorage [id-device]
  (str "device/" id-device))

(defn convert-path-file-to-path-url [path-file]
  (str/replace path-file path-filestorage path-url-filestorage))

(defn convert-path-url-to-path-file [path-file]
  (str/replace path-file path-url-filestorage path-filestorage))

(def pattern-device-id-in-path-url
  (re-pattern (str path-url-filestorage "/device/([0-9]+)/.*?")))

(defn get-id-device-from-path-url [path-url]
  (let [result (re-seq pattern-device-id-in-path-url path-url)]
    (-> result first second read-string)))

(defn build-dir-for-device [id-device]
  (str path-filestorage "/" (build-dir-for-device-after-filestorage id-device)))

(defn create-file [file-input filename id-device]
  (let [str-datetime #_(System/currentTimeMillis) (time.format/unparse timeformat-datetime-with-millis (time/now))
        path-dir-afetr-filestorage (str (build-dir-for-device-after-filestorage id-device) "/" str-datetime)
        path-file-after-filestorage (str path-dir-afetr-filestorage "/" filename)
        path-dir (str path-filestorage "/" path-dir-afetr-filestorage)
        path-file (str path-filestorage "/" path-file-after-filestorage)]
    (.mkdirs (io/file path-dir))
    (io/copy file-input (io/file path-file)) ; TODO avoid overwriting
    (str path-url-filestorage "/" path-file-after-filestorage)))

(defn split-list-by-page-params [list-to-split {:keys [limit page]}]
  (let [limit (if (nil? limit) 20 limit)
        page (if (nil? page) 0 page)]
    (->> list-to-split
         (split-at (* limit page))
         second
         (split-at limit)
         first)))

(defn get-list-with-total-for-user-device [params id-user id-device & [{:keys [transaction]}]]
  (when-let [device (model.device/get-by-id-for-user id-device id-user {:transaction transaction})]
    (let [list-files (->> (build-dir-for-device (:id device)) io/file file-seq
                          (map (fn [item] (when (.isFile item) (.getPath item))))
                          (remove nil?)
                          (map convert-path-file-to-path-url)
                          (sort #(compare %2 %1)))]
      {:list (->> list-files
                  (#(split-list-by-page-params % params))
                  (map (fn [path-url] {:path path-url
                                       :device_id (get-id-device-from-path-url path-url)})))
       :total (count list-files)
       :device device})))

(defn get-path-file-for-user [path-url id-user]
  (let [id-device (get-id-device-from-path-url path-url)
        device (model.device/get-by-id-for-user id-device id-user)]
    (when device
      (convert-path-url-to-path-file path-url))))
