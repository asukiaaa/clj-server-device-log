(ns front.view.util.label-lang.en
  (:require [goog.string :refer [format]]))

(def ^:private words
  {:action "Action"
   :active-watch-scope "Active watch scope"
   :add-term "Add term"
   :config-on-user-team "Config on user team"
   :create "Create"
   :created-at "Created at"
   :data "Data"
   :date "Date"
   :delete "Delete"
   :delete-term "Delete term"
   :device "Device"
   :devices "Devices"
   :device-type "Device type"
   :device-types "Device types"
   :device-type-config "Device type config"
   :device-type-config-on-user-team "Device type config on user teamlogin"
   :edit "Edit"
   :element "Element"
   :email "Email"
   :end "End"
   :home "Home"
   :id "ID"
   :indefinite-term "Indefinite term"
   :login "Login"
   :login-and-show-this-page "Login and show this page"
   :logout "Logout"
   :name "Name"
   :no-data "No data"
   :member "Member"
   :members "Members"
   :order-by-device "Order by device"
   :order-by-watch-scope "Order by watch scope"
   :page-not-found "Page not found"
   :password "Password"
   :password-edit "Edit password"
   :permission "Permission"
   :profile "Profile"
   :search "Search"
   :select "Select"
   :show "Show"
   :show-password "Show password"
   :start "Start"
   :term "Term"
   :terms "Terms"
   :term-of-watch-scope "Term of watch scope"
   :time "Time"
   :timezone "Timezone"
   :update "Update"
   :updated-at "Updated at"
   :user "User"
   :user-team "User team"
   :user-teams "User teams"
   :user-team-config "User team config"
   :user-team-configs "User team configs"
   :users "Users"
   :value "Value"
   :watch-scope "Watch scope"
   :watch-scopes "Watch scopes"})

(defn build-words []
  words)

(def ^:private fns
  {:create-watch-scope-for-user-team
   (fn [name-watch-scope name-user-team]
     (format "Create \"%s\" as %s of %s"
             (or name-watch-scope "")
             (:watch-scope words)
             (or name-user-team "")))})

(defn build-fns []
  fns)
