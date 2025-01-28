(ns back.models.device-file
  (:refer-clojure :exclude [update])
  (:require [clojure.java.io :as io]
            [clojure.core :refer [re-pattern re-seq]]
            [clojure.string :as str]
            [clj-time.core :as time]
            [clj-time.format :as time.format]
            [back.config :refer [path-filestorage db-spec]]
            [back.models.device :as model.device]
            [clojure.java.jdbc :as jdbc]))

(def timeformat-datetime-with-millis (time.format/formatter "yyyyMMdd-HHmmss-SSS"))
(def path-url-filestorage "/filestorage")

(defn build-dir-for-device-after-filestorage [id-device]
  (str "device/" id-device))

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

(defn get-path-files-for-device [id-device]
  (->> (build-dir-for-device id-device) io/file file-seq
       (map (fn [item] (when (.isFile item) (.getPath item))))
       (remove nil?)))

(defn build-info-map-from-path-file [path-file]
  (let [path-url (convert-path-file-to-path-url path-file)
        id-device (get-id-device-from-path-url path-url)
        created-at (-> path-url
                       get-created-at-from-path-url
                       convert-timestamp-on-path-to-timestamp-ordinal)]
    {:path path-url
     :device_id id-device
     :created_at created-at}))

(defn- assign-info-to-item-from-map [item {:keys [map-id-device]}]
  (let [device (get map-id-device (:device_id item))]
    (assoc item :device device)))

(defn- assign-info-to-list [list-files {:keys [transaction]}]
  (let [ids-device (->> list-files (map :device_id) distinct)
        devices (model.device/get-list-by-ids ids-device {:transaction transaction})
        map-id-device (into {} (for [device devices] [(:id device) device]))
        arr-info-file (->> list-files
                           (map #(assign-info-to-item-from-map % {:map-id-device map-id-device})))]
    arr-info-file))

(defn get-list-with-total-for-user-device [params id-user id-device & [{:keys [transaction]}]]
  (when-let [device (model.device/get-by-id-for-user id-device id-user {:transaction transaction})]
    (let [list-files (->> (get-path-files-for-device (:id device))
                          (map build-info-map-from-path-file)
                          (sort-by :created_at)
                          reverse)]
      {:list (assign-info-to-list list-files {:transaction transaction})
       :total (count list-files)
       :device device})))

(defn get-path-file-for-user [path-url id-user]
  (let [id-device (get-id-device-from-path-url path-url)
        device (model.device/get-by-id-for-user id-device id-user)]
    (when device
      (convert-path-url-to-path-file path-url))))

(defn get-list-with-total-latest-each-device [params sql-ids-user-team & [{:keys [transaction]}]]
  (let [ids-device (->> (jdbc/query (or transaction db-spec) sql-ids-user-team)
                        (map :id))
        arr-info-file
        (->> (for [id-device ids-device]
               (->> (for [path-file (-> id-device
                                        get-path-files-for-device)]
                      (build-info-map-from-path-file path-file))
                    (sort-by :created_at) reverse first))
             (remove nil?))
        total (count arr-info-file)]
    {:total total
     :list (assign-info-to-list arr-info-file {:transaction transaction})}))
