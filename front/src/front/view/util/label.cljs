(ns front.view.util.label
  (:require [goog.string :refer [format]])
  (:refer-clojure :exclude [name next time update]))

(def logout "Logout")
(def login "Login")

(def no-data "No data")
(def name "Name")

(defn- show-key [item keys]
  (if (empty? item)
    no-data
    (->> (for [key (if (keyword? keys) [keys] keys)]
           (str (if (keyword? key) (key item) (get-in item key))))
         (remove empty?)
         first
         (#(or % no-data)))))

(def action "Action")
(def active-watch-scope "Active watch scope")
(def add-term "Add term")
(def assign-device-to-user-team-to-list-up
  "Assign device to user team to list up")
(def authorization-bearer "Authorizatino bearer")
(def api-keys "API keys")
(def api-key "API key")
(defn api-key-item [item] (show-key item [:name :id]))
(def close "Close")
(def config-default "Config default")
(def config-format "Config format")
(def config-on-user-team "Config on user team")
(def copy "Copy")
(def copied "Copied")
(def create "Create")
(def created-at "Created at")
(def dashboard "Dashboard")
(defn datetime-from-item [str-datetiem-from]
  (str "from " str-datetiem-from))
(defn datetime-until-item [str-datetiem-from]
  (str "until " str-datetiem-from))
(def delete "Delete")
(def delete-term "Delete term")
(def device "Device")
(def devices "Devices")
(defn device-item [device] (show-key device [:name :id]))
(def device-types "Device types")
(def device-type "Device type")
(def device-type-config "Device type config")
(defn device-type-item [device-type] (show-key device-type [:name :id]))
(def device-groups "Device groups")
(def edit "Edit")
(def email "Email")
(def fetching "Fetching")
(def files "Files")
(def from "From")
(def get-bearer "Get bearer")
(def hide "Hide")
(def id "ID")
(def invalid-date "Invalid date")
(def logs "Logs")
(def next "Next")
(def no-file-to-show "No file to show")
(def no-term "No term")
(def manager-user-team "Manager user team")
(def member "Member")
(def memo "Memo")
(def owner-user "Owner user")
(def page-not-found "Page not found")
(def password "Password")
(def password-10-chars-or-more "Password 10 chars or more")
(def password-again "Password again")
(def password-edit "Edit password")
(def permission "Permission")
(def prev "Prev")
(def profile "Profile")
(def reset-password "Reset password")
(defn result-in-total [number-result total]
  (format "Result %d in %d" number-result total))
(def select-team "Select team")
(def show "Show")
(def show-password "Show password")
(def term "Term")
(def terms "Terms")
(def time "Time")
(def timezone "Timezone")
(def total "Total")
(def until "Until")
(def update "Update")
(def updated-at "Updated at")
(defn user-item [user] (show-key user [:name :email :id]))
(def users "Users")
(def user-team "User team")
(defn user-team-item [item] (show-key item [:name]))
(def user-teams "User teams")
(def user-team-config "User team config")
(def user-team-configs "User team configs")
(defn user-team-member-item [item] (show-key item [[:member :name]]))
(def watch-scope "Watch scope")
(def watch-scopes "Watch scopes")
(defn watch-scope-item [item] (show-key item [:name :id]))
