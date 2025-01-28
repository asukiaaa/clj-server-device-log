(ns front.route
  (:require [clojure.string :refer [includes?]]))

(def front "/front")
(def dashboard front)
(def login (str front "/login"))
(def logout (str front "/logout"))

(def profile (str front "/profile"))
(def profile-password-edit (str profile "/password_edit"))

(def users (str front "/users"))
(def user-create (str users "/create"))
(defn user-show [id] (str users "/" id))
(defn user-edit [id] (str (user-show id) "/edit"))

(defn user-password-reset [id hash]
  (str (user-show id) "/password_reset/" hash))

(def user-teams (str front "/user_teams"))
(def user-team-create (str user-teams "/create"))
(defn user-team-show [id] (str user-teams "/" id))
(defn user-team-edit [id] (str (user-team-show id) "/edit"))

(def devices (str front "/devices"))
(def device-create (str devices "/create"))
(defn device-show [id] (str devices "/" id))
(defn device-edit [id] (str (device-show id) "/edit"))
(defn device-raw-device-logs [id-device] (str (device-show id-device) "/raw_device_logs"))
(defn device-device-files [id] (str (device-show id) "/device_files"))

(def device-types (str front "/device_types"))
(def device-type-create (str device-types "/create"))
(defn device-type-show [id] (str device-types "/" id))
(defn device-type-edit [id] (str (device-type-show id) "/edit"))

(defn device-type-raw-device-logs [id-device-type]
  (str (device-type-show id-device-type) "/raw_device_logs"))
(defn device-type-device-type-api-keys [id-device-type]
  (str (device-type-show id-device-type) "/device_type_api_keys"))
(defn device-type-device-type-api-key-create [id-device-type]
  (str (device-type-device-type-api-keys id-device-type) "/create"))
(defn device-type-device-type-api-key-show [id-device-type id-device-type-api-key]
  (str (device-type-device-type-api-keys id-device-type) "/" id-device-type-api-key))
(defn device-type-device-type-api-key-edit [id-device-type id-device-type-api-key]
  (str (device-type-device-type-api-key-show id-device-type id-device-type-api-key) "/edit"))

(def watch-scopes (str front "/watch_scopes"))
(def watch-scope-create (str watch-scopes "/create"))
(defn watch-scope-show [id] (str watch-scopes "/" id))
(defn watch-scope-edit [id] (str (watch-scope-show id) "/edit"))

(defn watch-scope-watch-scope-terms [id-watch-scope]
  (str (watch-scope-show id-watch-scope) "/watch_scope_terms"))
(defn watch-scope-watch-scope-term-create [id-watch-scope]
  (str (watch-scope-watch-scope-terms id-watch-scope) "/create"))
(defn watch-scope-watch-scope-term-show [id-watch-scope id-watch-scope-term]
  (str (watch-scope-watch-scope-terms id-watch-scope) "/" id-watch-scope-term))
(defn watch-scope-watch-scope-term-edit [id-watch-scope id-watch-scope-term]
  (str (watch-scope-watch-scope-term-show id-watch-scope id-watch-scope-term) "/edit"))

(defn watch-scope-raw-device-logs [id-watch-scope]
  (str (watch-scope-show id-watch-scope) "/raw_device_logs"))

(defn show-login-page-when-not-loggedin [path]
  (and (includes? path front)
       (not (includes? path login))
       #_(not )))
