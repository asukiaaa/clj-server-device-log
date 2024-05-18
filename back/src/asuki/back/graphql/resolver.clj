(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]
            [asuki.back.models.user :as model.user]
            [com.walmartlabs.lacinia.resolve :as resolve]))

(defn raw-device-logs
  [_ args _]
  (println "args for raw-device-logs" args)
  (model-raw-device-log/get-list-with-total args))

(defn login [context args _]
  (println "args for login" args)
  (let [; error-login (:error-login) ; TODO
        user-loggedin-now (:user-loggedin-now context)]
    user-loggedin-now))

(defn get-user-loggedin [context]
  (:user-loggedin context))

(defn logout [_ _ _]
  (println "received logout request")
  true)

(defn user-loggedin [context args _]
  (println "args for user-loggedin" args)
  (let [user-loggedin (get-user-loggedin context)]
    #_(println context-user-loggedin)
    (when-let [id (:id user-loggedin)]
      (model.user/get-by-id id))))

(defn users [context args _]
  (println "args for users" args)
  (let [user-loggedin (get-user-loggedin context)]
    ; TODO show only admin
    (when-not (empty? user-loggedin)
      (model.user/get-list-with-total args))))

(defn user [context args _]
  (println "args for user" args)
  (let [user-loggedin (get-user-loggedin context)
        id-user (:id args)]
   ; TODO show only admin
    (when (and (seq user-loggedin) (integer? id-user))
      (model.user/get-by-id id-user))))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/users users
   :Query/user user
   :Query/user_loggedin user-loggedin
   :Mutation/login login
   :Mutation/logout logout})
