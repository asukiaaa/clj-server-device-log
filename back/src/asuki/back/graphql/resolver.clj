(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]
            [asuki.back.models.user :as model.user]
            [asuki.back.models.device-group :as model.device-group]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]))

(defn get-user-loggedin [context]
  (:user-loggedin context))

(defn handle-only-for-admin [context fn-to-handle]
  (if (model.user/admin? (get-user-loggedin context))
    (fn-to-handle)
    (resolve-as nil {:message "no permission to handle"})))

(defn raw-device-logs
  [_ args _]
  (println "args for raw-device-logs" args)
  (model-raw-device-log/get-list-with-total args))

(defn login [context args _]
  (println "requested user login")
  #_(println "args for login" args)
  (:user-loggedin context))

(defn logout [_ _ _]
  (println "received logout request")
  true)

(defn user-loggedin [context args _]
  (println "args for user-loggedin" args)
  (let [user-loggedin (get-user-loggedin context)]
    (when-let [id (:id user-loggedin)]
      (model.user/get-by-id id))))

(defn users [context args _]
  (println "args for users" args)
  (handle-only-for-admin
   context
   (fn [] (model.user/get-list-with-total args))))

(defn user [context args _]
  (println "args for user" args)
  (handle-only-for-admin
   context
   (fn []
     (when-let [id-user (:id args)]
       (model.user/get-by-id id-user)))))

(defn user-create [context args _]
  (println "handle user-create" (-> args :user :email))
  #_(println "args user-create" args)
  (handle-only-for-admin
   context
   (fn []
     (let [user-args (:user args)
           user-create-result (model.user/create-with-password user-args)
           user (:user user-create-result)
           errors (:errors user-create-result)]
       (if (seq user)
         {:user user}
         {:errors errors})))))

(defn user-update [context args _]
  (println "handle user-update")
  #_(println "args user-edit" args)
  (handle-only-for-admin
   context
   (fn []
     (let [user-args (:user args)
           user-id (:id args)
           user-update-result (model.user/update user-id user-args)
           user (:user user-update-result)
           errors (:errors user-update-result)]
       (if (seq user)
         {:user user}
         {:errors errors})))))

(defn user-delete [context args _]
  (println "args user-delete" args)
  (handle-only-for-admin
   context
   (fn []
     (let [user-id (:id args)]
       (model.user/delete user-id)
       user-id))))

(defn device-groups-for-user [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-group/get-list-with-total-for-user args (:id user))))

(defn device-group-for-user [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-group/get-by-id-for-user (:id args) (:id user))))

(defn device-group-create-for-user [context args _]
  (println "args device-group-create-for-user" args)
  (let [user (get-user-loggedin context)]
    (model.device-group/create {:name (:name args) :user_id (:id user)})))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/users users
   :Query/user user
   :Query/device_groups device-groups-for-user
   :Query/device_group device-group-for-user
   :Query/user_loggedin user-loggedin
   :Mutation/user_create user-create
   :Mutation/user_update user-update
   :Mutation/user_delete user-delete
   :Mutation/device_group_create device-group-create-for-user
   :Mutation/login login
   :Mutation/logout logout})
