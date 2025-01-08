(ns back.graphql.resolver
  (:require [back.models.raw-device-log :as model-raw-device-log]
            [back.models.user :as model.user]
            [back.models.device :as model.device]
            [back.models.device-group :as model.device-group]
            [back.models.device-group-api-key :as model.device-group-api-key]
            [back.models.device-watch-group :as model.device-watch-group]
            [back.models.device-watch-group-device :as model.device-watch-group-device]
            [back.models.device-file :as model.device-file]
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
  (model-raw-device-log/get-list-with-total args {:str-where-and "device_id IS NULL"}))

(defn raw-device-logs-for-device
  [_ args _]
  (println "args for raw-device-logs-for-device" args)
  (when-let [device-id (:device_id args)]
    (model-raw-device-log/get-list-with-total args {:str-where-and (format "device_id = %d" device-id)})))

(defn raw-device-logs-for-device-group
  [_ args _]
  (println "args for raw-device-logs-for-device-group" args)
  (when-let [device-group-id (:device_group_id args)]
    (model-raw-device-log/get-list-with-total args {:str-where-and (format "device_group.id = %d" device-group-id)})))

(defn raw-device-logs-for-device-watch-group
  [context args _]
  (println "args for raw-device-logs-for-device-watch-group" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (when-let [id-device-watch-group (:device_watch_group_id args)]
        (let [query-device-ids (model.device-watch-group-device/build-query-device-ids-for-device-watch-group id-device-watch-group)]
          (model-raw-device-log/get-list-with-total args {:str-where-and (format "device_id IN %s" query-device-ids)}))))))

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

(defn devices-for-user [context args _]
  (let [user (get-user-loggedin context)]
    (model.device/get-list-with-total-for-user args (:id user))))

(defn device-groups-for-user [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-group/get-list-with-total-for-user args (:id user))))

(defn device-group-for-user [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-group/get-by-id-for-user (:id args) (:id user))))

(defn device-group-for-user-create [context args _]
  (println "args device-group-for-user-create" args)
  (let [user (get-user-loggedin context)
        params_device_group (-> (:device_group args)
                                (assoc :user_id (:id user)))]
    (model.device-group/create params_device_group)))

(defn device-group-for-user-update [context args _]
  (println "args device-group-for-user-create" args)
  (let [user (get-user-loggedin context)]
    (model.device-group/for-user-update {:id (:id args)
                                         :id-user (:id user)
                                         :params (:device_group args)})))

(defn device-group-for-user-delete [context args _]
  (println "args device-group-for-user-delete" args)
  (let [user (get-user-loggedin context)]
    (model.device-group/for-user-delete {:id (:id args)
                                         :id-user (:id user)})))

(defn device-group-api-keys-for-device-group
  [context args _]
  (println "args for device-group-api-keys-for-device-group" args)
  (let [user (get-user-loggedin context)]
    (when-let [id-device-group (:device_group_id args)]
      (model.device-group-api-key/get-list-with-total-for-user-and-device-group
       args (:id user) id-device-group))))

(defn device-group-api-key-for-device-group
  [context args _]
  (println "args for device-group-api-key-for-device-group" args)
  (let [user (get-user-loggedin context)]
    (when-let [id-device-group (:device_group_id args)]
      (model.device-group-api-key/get-by-id-for-user-and-device-group
       (:id args) (:id user) id-device-group))))

(defn device-group-api-key-for-user-create [context args _]
  (println "args device-group-api-key-for-user-create" args)
  (let [user (get-user-loggedin context)
        params (:device_group_api_key args)]
    (model.device-group-api-key/create-for-user params (:id user))))

(defn device-group-api-key-for-user-update [context args _]
  (println "args device-group-api-key-for-user-update" args)
  (let [user (get-user-loggedin context)
        params (:device_group_api_key args)]
    (model.device-group-api-key/update-for-user
     {:params params
      :id (:id args)
      :id-user (:id user)})))

(defn device-group-api-key-for-user-delete [context args _]
  (println "args device-group-api-key-for-user-delete" args)
  (let [user (get-user-loggedin context)]
    (model.device-group-api-key/delete-for-user {:id (:id args)
                                                 :id-user (:id user)})))

(defn device-files-for-device
  [context args _]
  (println "args for device-files-for-device" args)
  (let [user (get-user-loggedin context)
        id-device (:device_id args)]
    (model.device-file/get-list-with-total-for-user-device args (:id user) id-device)))

(defn device-watch-groups
  [context args _]
  (println "args for device-watch-groups" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device-watch-group/get-list-with-total-for-admin args))))

(defn device-watch-group
  [context args _]
  (println "args for device-watch-group" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device-watch-group/get-by-id (:id args)))))

(defn device-watch-group-create [context args _]
  (println "args device-watch-group-create" args)
  (let [user (get-user-loggedin context)
        params (:device_watch_group args)]
    (when (model.user/admin? user) (model.device-watch-group/create params))))

(defn device-watch-group-update [context args _]
  (println "args device-watch-group-update" args)
  (let [user (get-user-loggedin context)
        id (:id args)
        params (:device_watch_group args)]
    (when (model.user/admin? user) (model.device-watch-group/update id params))))

(defn device-watch-group-delete [context args _]
  (println "args device-watch-group-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.device-watch-group/delete id))))

(defn device-watch-group-devices-for-device-watch-group
  [context args _]
  (println "args for device-watch-group-devices" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device-watch-group-device/get-list-with-total-for-admin args))))

(defn device-watch-group-device-for-device-watch-group
  [context args _]
  (println "args for device-watch-group-device" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device-watch-group-device/get-by-id (:id args)))))

(defn device-watch-group-device-create [context args _]
  (println "args device-watch-group-device-create" args)
  (let [user (get-user-loggedin context)
        params (:device_watch_group_device args)]
    (when (model.user/admin? user) (model.device-watch-group-device/create params))))

(defn device-watch-group-device-update [context args _]
  (println "args device-watch-group-device-update" args)
  (let [user (get-user-loggedin context)
        id (:id args)
        params (:device_watch_group_device args)]
    (when (model.user/admin? user) (model.device-watch-group-device/update id params))))

(defn device-watch-group-device-delete [context args _]
  (println "args device-watch-group-device-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.device-watch-group-device/delete id))))

(defn device-for-user-create [context args _]
  (println "args device-for-user-create" args)
  (let [user (get-user-loggedin context)
        params_device_group (:device args)]
    (model.device/create-for-user params_device_group (:id user))))

(defn device-for-user [context args _]
  (println "args device-for-user" args)
  (let [user (get-user-loggedin context)]
    (model.device/get-by-id-for-user (:id args) (:id user))))

(defn device-for-user-update [context args _]
  (println "args device-for-user-update" args)
  (let [user (get-user-loggedin context)
        params (-> args :device)]
    (model.device/for-user-update {:id (:id args) :id-user (:id user) :params params})))

(defn device-for-user-delete [context args _]
  (println "args device-for-user-delete" args)
  (let [user (get-user-loggedin context)]
    (model.device/for-user-delete {:id (:id args) :id-user (:id user)})))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/raw_device_logs_for_device raw-device-logs-for-device
   :Query/raw_device_logs_for_device_group raw-device-logs-for-device-group
   :Query/raw_device_logs_for_device_watch_group raw-device-logs-for-device-watch-group
   :Query/device_group_api_keys_for_device_group device-group-api-keys-for-device-group
   :Query/device_group_api_key_for_device_group device-group-api-key-for-device-group
   :Query/device_watch_groups device-watch-groups
   :Query/device_watch_group device-watch-group
   :Query/device_watch_group_devices_for_device_watch_group device-watch-group-devices-for-device-watch-group
   :Query/device_watch_group_device_for_device_watch_group device-watch-group-device-for-device-watch-group
   :Query/device_files_for_device device-files-for-device
   :Query/users users
   :Query/user user
   :Query/devices devices-for-user
   :Query/device device-for-user
   :Query/device_groups device-groups-for-user
   :Query/device_group device-group-for-user
   :Query/user_loggedin user-loggedin
   :Mutation/user_create user-create
   :Mutation/user_update user-update
   :Mutation/user_delete user-delete
   :Mutation/device_for_user_create device-for-user-create
   :Mutation/device_for_user_update device-for-user-update
   :Mutation/device_for_user_delete device-for-user-delete
   :Mutation/device_group_for_user_create device-group-for-user-create
   :Mutation/device_group_for_user_update device-group-for-user-update
   :Mutation/device_group_for_user_delete device-group-for-user-delete
   :Mutation/device_group_api_key_for_user_create device-group-api-key-for-user-create
   :Mutation/device_group_api_key_for_user_update device-group-api-key-for-user-update
   :Mutation/device_group_api_key_for_user_delete device-group-api-key-for-user-delete
   :Mutation/device_watch_group_create device-watch-group-create
   :Mutation/device_watch_group_update device-watch-group-update
   :Mutation/device_watch_group_delete device-watch-group-delete
   :Mutation/device_watch_group_device_create device-watch-group-device-create
   :Mutation/device_watch_group_device_update device-watch-group-device-update
   :Mutation/device_watch_group_device_delete device-watch-group-device-delete
   :Mutation/login login
   :Mutation/logout logout})
