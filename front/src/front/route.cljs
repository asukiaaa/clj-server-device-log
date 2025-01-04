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
