(ns front.route)

(def front "/front")
(def dashboard front)
(def login (str front "/login"))

(def profile (str front "/profile"))

(def users (str front "/users"))
(def user-create (str users "/create"))
(defn user-show [id] (str users "/" id))
(defn user-edit [id] (str (user-show id) "/edit"))

(def devices (str front "/devices"))
(def device-create (str devices "/create"))
(defn device-show [id] (str devices "/" id))
(defn device-edit [id] (str (device-show id) "/edit"))
(defn device-raw-device-logs [id-device] (str (device-show id-device) "/raw_device_logs"))
(defn device-device-files [id] (str (device-show id) "/device_files"))

(def device-groups (str front "/device_groups"))
(def device-group-create (str device-groups "/create"))
(defn device-group-show [id] (str device-groups "/" id))
(defn device-group-edit [id] (str (device-group-show id) "/edit"))

(defn device-group-raw-device-logs [id-device-group]
  (str (device-group-show id-device-group) "/raw_device_logs"))
(defn device-group-device-group-api-keys [id-device-group]
  (str (device-group-show id-device-group) "/device_group_api_keys"))
(defn device-group-device-group-api-key-create [id-device-group]
  (str (device-group-device-group-api-keys id-device-group) "/create"))
(defn device-group-device-group-api-key-show [id-device-group id-device-group-api-key]
  (str (device-group-device-group-api-keys id-device-group) "/" id-device-group-api-key))
(defn device-group-device-group-api-key-edit [id-device-group id-device-group-api-key]
  (str (device-group-device-group-api-key-show id-device-group id-device-group-api-key) "/edit"))

(def device-watch-groups (str front "/device_watch_groups"))
(def device-watch-group-create (str device-watch-groups "/create"))
(defn device-watch-group-show [id] (str device-watch-groups "/" id))
(defn device-watch-group-edit [id] (str (device-watch-group-show id) "/edit"))

(defn device-watch-group-device-watch-group-devices [id-device-watch-group]
  (str (device-watch-group-show id-device-watch-group) "/device_watch_group_devices"))
(defn device-watch-group-device-watch-group-device-create [id-device-watch-group]
  (str (device-watch-group-device-watch-group-devices id-device-watch-group) "/create"))
(defn device-watch-group-device-watch-group-device-show [id-device-watch-group id-device-watch-group-device]
  (str (device-watch-group-device-watch-group-devices id-device-watch-group) "/" id-device-watch-group-device))
(defn device-watch-group-device-watch-group-device-edit [id-device-watch-group id-device-watch-group-device]
  (str (device-watch-group-device-watch-group-device-show id-device-watch-group id-device-watch-group-device) "/edit"))

(defn device-watch-group-raw-device-logs [id-device-watch-group]
  (str (device-watch-group-show id-device-watch-group) "/raw_device_logs"))
