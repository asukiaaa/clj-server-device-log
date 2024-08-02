(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]
            [asuki.back.models.user :as model.user]
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
  (println "args user-create" args)
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

(defn user-edit [context args _]
  (println "args user-edit" args)
  (handle-only-for-admin
   context
   (fn []
     (let [user-args (:user args)
           user-id (:id args)
           user-create-result (model.user/update user-id user-args)
           user (:user user-create-result)
           errors (:errors user-create-result)]
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

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/users users
   :Query/user user
   :Query/user_loggedin user-loggedin
   :Mutation/userCreate user-create
   :Mutation/userEdit user-edit
   :Mutation/userDelete user-delete
   :Mutation/login login
   :Mutation/logout logout})
