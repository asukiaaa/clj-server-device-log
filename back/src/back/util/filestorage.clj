(ns back.util.filestorage
  (:require [clj-time.core :as time]
            [clj-time.format :as time.format]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [back.config :refer [path-filestorage]]))

(def timeformat-datetime-with-millis (time.format/formatter "yyyyMMdd-HHmmss-SSS"))
(def path-url-filestorage "/filestorage")

(defn- build-path-dir-for-devices-after-filestorage []
  "device/")

(defn- build-path-dir-for-device-after-filestorage [id-device]
  (str (build-path-dir-for-devices-after-filestorage) id-device))

(defn build-dir-for-device [id-device]
  (str path-filestorage "/" (build-path-dir-for-device-after-filestorage id-device)))

(defn build-dir-for-devices []
  (str path-filestorage "/" (build-path-dir-for-devices-after-filestorage)))

(defn- build-path-file-for-device-after-filestorage [item]
  (let [id-device (:device_id item)
        str-datetime (:datetime_dir item)
        name (:name item)]
    (str (build-path-dir-for-device-after-filestorage id-device) "/" str-datetime "/" name)))

(defn build-path-url-for-device [item]
  (str path-url-filestorage "/" (build-path-file-for-device-after-filestorage item)))

(defn convert-path-file-to-path-url [path-file]
  (str/replace path-file path-filestorage path-url-filestorage))

(defn convert-path-url-to-path-file [path-url]
  (str/replace path-url path-url-filestorage path-filestorage))

(def pattern-device-id-in-path-url
  (re-pattern (str path-url-filestorage "/device/([0-9]+)/.*?")))

(defn get-id-device-from-path-url [path-url]
  (let [result (re-seq pattern-device-id-in-path-url path-url)
        str-id (-> result first second)]
    (when-not (nil? str-id) (read-string str-id))))

(def pattern-created-at-in-path-url
  (re-pattern (str path-url-filestorage "/device/[0-9]+/([0-9-]+)/.*?")))

#_(def pattern-timestamp-on-path
    (re-pattern (str path-url-filestorage)))

(defn convert-timestamp-on-path-to-timestamp-ordinal [timestamp-on-path]
  (when-let [result (re-matches #"(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})(\d{2})-(\d{3})"  timestamp-on-path)]
    (let [[_ year month day hour min sec millis] result]
      (str year "-" month "-" day " " hour ":" min ":" sec "." millis))))

(defn get-created-at-from-path-url [path-url]
  (let [result (re-seq pattern-created-at-in-path-url path-url)]
    (-> result first second)))

(defn- build-path-url-of-item [item]
  (let [id-device (:device_id item)
        str-datetime (:datetime_dir item)
        name (:name item)]
    (str (build-path-dir-for-device-after-filestorage id-device) "/" str-datetime "/" name)))

(defn- get-path-files [dir]
  (->> dir io/file file-seq
       (map (fn [item] (when (.isFile item) (.getPath item))))
       (remove nil?)))

(defn get-path-files-for-device [id-device]
  (get-path-files (build-dir-for-device id-device)))

(defn get-path-files-for-devices [id-device]
  (get-path-files (build-dir-for-devices)))

(defn create-file-for-device [file-input filename id-device]
  (let [str-datetime (time.format/unparse timeformat-datetime-with-millis (time/now))
        path-dir-afetr-filestorage (str (build-path-dir-for-device-after-filestorage id-device) "/" str-datetime)
        path-file-after-filestorage (str path-dir-afetr-filestorage "/" filename)
        path-dir (str path-filestorage "/" path-dir-afetr-filestorage)
        path-file (str path-filestorage "/" path-file-after-filestorage)
        params {:name filename
                :datetime_dir str-datetime
                :device_id id-device}]
    (.mkdirs (io/file path-dir))
    (io/copy file-input (io/file path-file)) ; TODO avoid overwriting
    params))

(defn build-info-map-from-path-file [path-file]
  (let [path-url (convert-path-file-to-path-url path-file)
        id-device (get-id-device-from-path-url path-url)
        created-at (-> path-url
                       get-created-at-from-path-url
                       convert-timestamp-on-path-to-timestamp-ordinal)]
    {:path path-url
     :device_id id-device
     :created_at created-at}))
