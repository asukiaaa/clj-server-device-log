(ns front.view.util.label
  (:require [goog.string :refer [format]]
            [cljs.reader]
            [front.view.util.label-lang.en :as lang.en]
            [front.view.util.label-lang.ja :as lang.ja])
  (:refer-clojure :exclude [name next time update]))

(defn- get-target-lang []
  (.-language js/navigator))

(defn- get-word-from-words [word-key]
  (let [lang-target (get-target-lang)]
    (or (cond
          (= lang-target "ja")
          (word-key (lang.ja/build-words)))
        (word-key (lang.en/build-words)))))

(defn- show-key [item keys]
  (let [str-no-data (get-word-from-words :no-data)]
    (if (empty? item)
      str-no-data
      (->> (for [key (if (keyword? keys) [keys] keys)]
             (str (if (keyword? key) (key item) (get-in item key))))
           (remove empty?)
           first
           (#(or % str-no-data))))))

(defn action [] (get-word-from-words :action))
(defn active-watch-scope [] (get-word-from-words :active-watch-scope))
(defn add-term [] (get-word-from-words :add-term))
(def assign-device-to-user-team-to-list-up
  "Assign device to user team to list up")
(def authorization-bearer "Authorizatino bearer")
(def api-keys "API keys")
(def api-key "API key")
(defn api-key-item [item] (show-key item [:name :id]))
(def close "Close")
(def config-default "Config default")
(def config-format "Config format")
(def config-renderer-default "Config renderer default")
(def config-on-user-team "Config on user team")
(def copy "Copy")
(def copied "Copied")
(defn create [] (get-word-from-words :create))
(def created-at "Created at")
(def dashboard "Dashboard")
(defn data [] (get-word-from-words :data))
(defn datetime-from-item [str-datetiem-from]
  (str "from " str-datetiem-from))
(defn datetime-until-item [str-datetiem-from]
  (str "until " str-datetiem-from))
(defn delete [] (get-word-from-words :delete))
(defn delete-term [] (get-word-from-words :delete-term))
(defn device [] (get-word-from-words :device))
(defn devices [] (get-word-from-words :devices))
(defn device-item [device] (show-key device [:name :id]))
(defn device-types [] (get-word-from-words :device-type))
(defn device-type [] (get-word-from-words :device-types))
(defn device-type-config [] (get-word-from-words :device-type-config))
(defn device-type-item [device-type] (show-key device-type [:name :id]))
(def device-groups "Device groups")
(defn- str->int-if-needed [val]
  (if (string? val) (cljs.reader/read-string val) val))
(defn display-page-limit-total [index-page number-limit total]
  (let [index-page (str->int-if-needed index-page)
        number-limit (str->int-if-needed number-limit)
        total (str->int-if-needed total)
        number-from (inc (* index-page number-limit))
        number-to (min (* (inc index-page) number-limit) total)]
    (cond
      (> number-to number-from)
      (format "%d to %d of total %d" number-from number-to total)
      (= number-to number-from)
      (format "%d of total %d" number-to total)
      :else
      (format "None of total %d" total))))
(defn edit [] (get-word-from-words :edit))
(def email "Email")
(def fetching "Fetching")
(def files "Files")
(def from "From")
(def get-bearer "Get bearer")
(def hide "Hide")
(def id "ID")
(defn indefinite-term [] (get-word-from-words :indefinite-term))
(def invalid-date "Invalid date")
(defn login [] (get-word-from-words :login))
(defn logout [] (get-word-from-words :logout))
(def logs "Logs")
(def next "Next")
(defn name [] (get-word-from-words :name))
(defn no-data [] (get-word-from-words :no-data))
(def no-file-to-show "No file to show")
(def manager-user-team "Manager user team")
(def member "Member")
(def members "Members")
(def memo "Memo")
(def owner-user "Owner user")
(def page-not-found "Page not found")
(def password "Password")
(def password-10-chars-or-more "Password 10 chars or more")
(def password-again "Password again")
(def password-edit "Edit password")
(def permission "Permission")
(def prev "Prev")
(defn profile [] (get-word-from-words :profile))
(def reset-password "Reset password")
(defn result-in-total [number-result total]
  (format "Result %d in %d" number-result total))
(def select-team "Select team")
(defn show [] (get-word-from-words :show))
(defn show-password [] (get-word-from-words :show-password))
(defn term [] (get-word-from-words :term))
(defn terms [] (get-word-from-words :terms))
(defn time [] (get-word-from-words :time))
(defn timezone [] (get-word-from-words :timezone))
(def total "Total")
(def until "Until")
(defn update [] (get-word-from-words :update))
(def updated-at "Updated at")
(defn user-item [user] (show-key user [:name :email :id]))
(defn users [] (get-word-from-words :users))
(defn user-team [] (get-word-from-words :user-team))
(defn user-team-item [item] (show-key item [:name]))
(defn user-teams [] (get-word-from-words :user-teams))
(defn user-team-config [] (get-word-from-words :user-team-config))
(defn user-team-configs [] (get-word-from-words :user-team-configs))
(defn user-team-member-item [item] (show-key item [[:member :name]]))
(defn watch-scope [] (get-word-from-words :watch-scope))
(defn watch-scopes [] (get-word-from-words :watch-scopes))
(defn watch-scope-item [item] (show-key item [:name :id]))
