(ns back.util.filestorage
  (:require [clj-time.core :as time]
            [clj-time.format :as time.format]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [back.config :refer [path-local-filestorage]]
            [back.util.time :refer [timeformat-datetime-with-millis]])
  (:import
   [java.awt Image]
   [java.awt.image BufferedImage]
   [java.io File]
   [javax.imageio ImageIO]))

(def path-url-filestorage "/filestorage")
(def path-url-filestorage-thumbnail (str path-url-filestorage "/thumbnail"))
(def path-local-filestorage-thumbnail (str path-local-filestorage "/thumbnail"))

(def thumbnail-width 600)
(def thumbnail-height 450)

(defn parse-str-datetime [str-datetime]
  (time.format/parse timeformat-datetime-with-millis str-datetime))

(defn- build-path-dir-for-devices-after-filestorage []
  "device/")

(defn- build-path-dir-for-device-after-filestorage [id-device]
  (str (build-path-dir-for-devices-after-filestorage) id-device))

(defn build-dir-for-device [id-device]
  (str path-local-filestorage "/" (build-path-dir-for-device-after-filestorage id-device)))

(defn build-dir-for-devices []
  (str path-local-filestorage "/" (build-path-dir-for-devices-after-filestorage)))

(defn- build-path-file-for-device-after-filestorage [item]
  (let [id-device (:device_id item)
        str-datetime (:datetime_dir item)
        name (:name item)]
    (str (build-path-dir-for-device-after-filestorage id-device) "/" str-datetime "/" name)))

(defn build-path-url-for-device [item]
  (str path-url-filestorage "/" (build-path-file-for-device-after-filestorage item)))

(defn build-path-local-for-device [item]
  (str path-local-filestorage "/" (build-path-file-for-device-after-filestorage item)))

(defn build-path-url-thumbnail-for-device [item]
  (str path-url-filestorage-thumbnail "/" (build-path-file-for-device-after-filestorage item)))

(defn build-path-local-thumbnail-for-device [item]
  (str path-local-filestorage-thumbnail "/" (build-path-file-for-device-after-filestorage item)))

(defn present-thumbnail-for-device? [item]
  (-> (build-path-local-thumbnail-for-device item) io/file .exists))

(defn convert-path-file-to-path-url [path-file]
  (str/replace path-file path-local-filestorage path-url-filestorage))

(defn convert-path-url-to-path-file [path-url]
  (str/replace path-url path-url-filestorage path-local-filestorage))

(defn convert-path-file-to-path-file-thumbnail [path-file]
  (str/replace path-file path-local-filestorage path-local-filestorage-thumbnail))

(def pattern-device-id-in-path-url
  (re-pattern (str path-url-filestorage "/device/([0-9]+)/.*?")))

(defn get-id-device-from-path-url [path-url]
  (let [result (re-seq pattern-device-id-in-path-url path-url)
        str-id (-> result first second)]
    (when-not (nil? str-id) (read-string str-id))))

(def pattern-device-id-in-path-url-thumbnail
  (re-pattern (str path-url-filestorage-thumbnail "/device/([0-9]+)/.*?")))

(defn get-id-device-from-path-url-thumbnail [path-url]
  (let [result (re-seq pattern-device-id-in-path-url-thumbnail path-url)
        str-id (-> result first second)]
    (when-not (nil? str-id) (read-string str-id))))

(def pattern-created-at-in-path-url
  (re-pattern (str path-url-filestorage "/device/[0-9]+/([0-9-]+)/.*?")))

(def pattern-filename-in-path-url
  (re-pattern (str path-url-filestorage "/device/[0-9]+/[0-9-]+/(.*+)")))

(defn convert-timestamp-on-path-to-timestamp-ordinal [timestamp-on-path]
  (when-let [result (re-matches #"(\d{4})(\d{2})(\d{2})-(\d{2})(\d{2})(\d{2})-(\d{3})"  timestamp-on-path)]
    (let [[_ year month day hour min sec millis] result]
      (str year "-" month "-" day " " hour ":" min ":" sec "." millis))))

(defn get-str-created-at-from-path-url [path-url]
  (let [result (re-seq pattern-created-at-in-path-url path-url)]
    (-> result first second)))

(defn get-filename-from-path-url [path-url]
  (let [result (re-seq pattern-filename-in-path-url path-url)]
    (-> result first second)))

(defn- get-path-files [dir]
  (->> dir io/file file-seq
       (map (fn [item] (when (.isFile item) (.getPath item))))
       (remove nil?)))

(defn get-path-files-for-device [id-device]
  (get-path-files (build-dir-for-device id-device)))

(defn get-path-files-for-devices []
  (get-path-files (build-dir-for-devices)))

(defn get-ids-device []
  (->> (build-dir-for-devices) io/file .list seq
       (map (fn [item] (Integer. item)))))

(defn- split-by-params [list-files params]
  (let [{:keys [limit page]} params]
    (->> list-files
         (split-at (* limit page))
         second
         (split-at limit)
         first)))

(defn get-extension-of-file [path-file]
  (second (re-find #"\.([^.]+)$" path-file)))

(defn is-path-image? [path-file]
  (let [extension (get-extension-of-file path-file)]
    (some #(= % extension) ["JPG" "JPEG" "jpeg" "jpg"
                            "PNG" "png"
                            "BMP" "bmp"])))

(defn image-resize-and-crop-to-fit [path-input path-output width height]
  (let [image-input (ImageIO/read (File. path-input))
        image-input-width (.getWidth image-input)
        image-input-height (.getHeight image-input)
        required-scale-by-height (/ (double height) image-input-height)
        required-scale-by-width (/ (double width) image-input-width)
        required-scale-bigger (max required-scale-by-width required-scale-by-height)]
    #_(println :origin-size image-input-width image-input-height)
    (when (< required-scale-bigger 1)
      (let [scaled-width (-> (* image-input-width required-scale-bigger) Math/ceil int)
            scaled-height (-> (* image-input-height required-scale-bigger) Math/ceil int)
            scaled-instance (.getScaledInstance image-input scaled-width scaled-height Image/SCALE_AREA_AVERAGING)
            scaled-image (BufferedImage. scaled-width scaled-height (.getType image-input))
            _ (-> scaled-image
                  .getGraphics
                  (.drawImage scaled-instance 0 0 nil))
            fn-get-position (fn [scaled-val target-val]
                              (-> (if (> scaled-val target-val)
                                    (- scaled-val target-val)
                                    (- target-val scaled-val))
                                  double
                                  (/ 2)
                                  int))
            cropped-x (fn-get-position scaled-width width)
            cropped-y (fn-get-position scaled-height height)
            ;_ (println :cropped-position cropped-x cropped-y)
            cropped-image (.getSubimage scaled-image cropped-x cropped-y width height) ; not getSubImage
            name-extension (get-extension-of-file path-output)]
        #_(println :scaled scaled-width scaled-height)
        (ImageIO/write cropped-image name-extension (File. path-output))))))

(defn present-thumbnail-of-file-local? [path-local]
  (-> (convert-path-file-to-path-file-thumbnail path-local) io/file .exists))

(defn create-thumbnail [path-local & [{:keys [force]}]]
  (when-not (or (present-thumbnail-of-file-local? path-local) force)
    (let [path-local-thumbnail (convert-path-file-to-path-file-thumbnail path-local)]
      (io/make-parents path-local-thumbnail)
      (image-resize-and-crop-to-fit path-local path-local-thumbnail thumbnail-width thumbnail-height))))

(defn create-file-for-device [file-input filename id-device]
  (let [str-datetime (time.format/unparse timeformat-datetime-with-millis (time/now))
        path-dir-afetr-filestorage (str (build-path-dir-for-device-after-filestorage id-device) "/" str-datetime)
        path-file-after-filestorage (str path-dir-afetr-filestorage "/" filename)
        path-dir (str path-local-filestorage "/" path-dir-afetr-filestorage)
        path-file (str path-local-filestorage "/" path-file-after-filestorage)
        params {:name filename
                :datetime_dir str-datetime
                :device_id id-device}]
    (.mkdirs (io/file path-dir))
    (io/copy file-input (io/file path-file)) ; TODO avoid overwriting
    (when (is-path-image? path-file)
      (create-thumbnail path-file))
    params))

(defn build-path-local-for-device-file [item]
  (str path-local-filestorage "/" (build-path-file-for-device-after-filestorage item)))

(defn build-info-map-from-path-file [path-file]
  (let [path-url (convert-path-file-to-path-url path-file)
        id-device (get-id-device-from-path-url path-url)
        filename (get-filename-from-path-url path-url)
        created-at (-> path-url
                       get-str-created-at-from-path-url
                       convert-timestamp-on-path-to-timestamp-ordinal)]
    {:path path-url
     :device_id id-device
     :name filename
     :created_at created-at}))
