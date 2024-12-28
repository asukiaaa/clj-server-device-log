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

(def device-groups (str front "/device_groups"))
(def device-group-create (str device-groups "/create"))
(defn device-group-show [id] (str device-groups "/" id))
(defn device-group-edit [id] (str (device-group-show id) "/edit"))
(defn device-group-raw-device-logs [id-device-group]
  (str (device-group-show id-device-group) "/raw_device_logs"))
