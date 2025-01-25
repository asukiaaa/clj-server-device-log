(ns front.view.util.label)

(def logout "Logout")
(def login "Login")

(def no-data "No data")

(defn- show-key [item keys]
  (if (string? keys)
    (show-key item [keys])
    (if (empty? item)
      no-data
      (->> (for [key keys] (str (key item)))
           (remove empty?)
           first
           (#(or % no-data))))))

(def dashboard "Dashboard")
(def devices "Devices")
(defn device [device] (show-key device [:name :id]))
(def device-watch-groups "Device watch groups")
(def device-groups "Device groups")
(def files "Files")
(def logs "Logs")
(def profile "Profile")
(def users "Users")
(defn user [user] (show-key user [:name :email :id]))
(def user-teams "User teams")

(def show "Show")
(def edit "Edit")
(def create "Create")
(def delete "Delete")
(def password-edit "Edit password")
