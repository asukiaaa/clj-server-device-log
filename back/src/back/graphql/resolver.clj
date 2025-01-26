(ns back.graphql.resolver
  (:require [clojure.java.jdbc :as jdbc]
            [back.config :refer [db-spec]]
            [back.models.raw-device-log :as model-raw-device-log]
            [back.models.user :as model.user]
            [back.models.user-team :as model.user-team]
            [back.models.device :as model.device]
            [back.models.device-type :as model.device-type]
            [back.models.device-type-api-key :as model.device-type-api-key]
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
  [context args _]
  (println "args for raw-device-logs-for-device" args)
  (when-let [id-device (:device_id args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [user (get-user-loggedin context)]
        (when-let [device (model.device/get-by-id-for-user id-device (:id user) {:transaction transaction})]
          (-> (model-raw-device-log/get-list-with-total
               args
               {:str-where-and (format "device_id = %d" id-device)
                :transaction transaction})
              (assoc :device device)))))))

(defn raw-device-logs-for-device-type
  [context args _]
  (println "args for raw-device-logs-for-device-type" args)
  (let [id-device-type (:device_type_id args)
        user (get-user-loggedin context)
        id-user (:id user)]
    (jdbc/with-db-transaction [transaction db-spec]
      (when-let [device-type (model.device-type/get-by-id-for-user id-device-type id-user {:transaction transaction})]
        (-> (model-raw-device-log/get-list-with-total args {:str-where-and (format "device_type.id = %d" id-device-type)
                                                            :transaction transaction})
            (assoc model.device-type/key-table device-type))))))

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

(defn user-for-resetting-password [context args _]
  (println "args for user-for-resetting-password" args)
  (let [id-user (:id args)
        hash-password-reset (:hash args)]
    (when-let [user (model.user/get-by-id-and-hash-password-reset id-user hash-password-reset)]
      user)))

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

(defn user-teams
  [context args _]
  (println "args for user-teams" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.user-team/get-list-with-total-for-admin args))))

(defn user-team
  [context args _]
  (println "args for user-team" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.user-team/get-by-id (:id args)))))

(defn user-team-create [context args _]
  (println "args user-team-create" args)
  (let [user (get-user-loggedin context)
        params (:user_team args)]
    (when (model.user/admin? user) (model.user-team/create params))))

(defn user-team-update [context args _]
  (println "args user-team-update" args)
  (let [user (get-user-loggedin context)
        id (:id args)
        params (:user_team args)]
    (when (model.user/admin? user) (model.user-team/update id params))))

(defn user-team-delete [context args _]
  (println "args user-team-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.user-team/delete id))))

(defn devices [context args _]
  (let [user (get-user-loggedin context)]
    (model.device/get-list-with-total-for-user args (:id user))))

(defn device-types [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-type/get-list-with-total-for-user args (:id user))))

(defn device-type [context args _]
  (let [user (get-user-loggedin context)]
    (model.device-type/get-by-id-for-user (:id args) (:id user))))

(defn device-type-create [context args _]
  (println "args device-type-create" args)
  (let [user (get-user-loggedin context)
        params_device_type (-> (:device_type args)
                               (assoc :user_id (:id user)))]
    (model.device-type/create params_device_type)))

(defn device-type-update [context args _]
  (println "args device-type-create" args)
  (let [user (get-user-loggedin context)]
    (model.device-type/for-user-update {:id (:id args)
                                        :id-user (:id user)
                                        :params (:device_type args)})))

(defn device-type-delete [context args _]
  (println "args device-type-delete" args)
  (let [user (get-user-loggedin context)]
    (model.device-type/for-user-delete {:id (:id args)
                                        :id-user (:id user)})))

(defn device-type-api-keys-for-device-type
  [context args _]
  (println "args for device-type-api-keys-for-device-type" args)
  (let [user (get-user-loggedin context)
        id-user (:id user)]
    (when-let [id-device-type (:device_type_id args)]
      (when-let [device-type (model.device-type/get-by-id-for-user id-device-type id-user)]
        (-> (model.device-type-api-key/get-list-with-total-for-user-and-device-type
             args id-user id-device-type)
            (assoc model.device-type/key-table device-type))))))

(defn device-type-api-key-for-device-type
  [context args _]
  (println "args for device-type-api-key-for-device-type" args)
  (let [user (get-user-loggedin context)]
    (when-let [id-device-type (:device_type_id args)]
      (model.device-type-api-key/get-by-id-for-user-and-device-type
       (:id args) (:id user) id-device-type))))

(defn device-type-api-key-create [context args _]
  (println "args device-type-api-key-create" args)
  (let [user (get-user-loggedin context)
        params (:device_type_api_key args)]
    (model.device-type-api-key/create-for-user params (:id user))))

(defn device-type-api-key-update [context args _]
  (println "args device-type-api-key-update" args)
  (let [user (get-user-loggedin context)
        params (:device_type_api_key args)]
    (model.device-type-api-key/update-for-user
     {:params params
      :id (:id args)
      :id-user (:id user)})))

(defn device-type-api-key-delete [context args _]
  (println "args device-type-api-key-delete" args)
  (let [user (get-user-loggedin context)]
    (model.device-type-api-key/delete-for-user {:id (:id args)
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

(defn hash-for-resetting-password-create [context args _]
  (println "args hash-for-resetting-password-create" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user)
      (model.user/create-hash-for-resetting-password id))))

(defn password-for-hash-user-reset [context args _]
  (println "args password-for-hash-user-reset" args)
  (Thread/sleep 1000) ; wait to take tome for brute force attack
  (model.user/reset-password-for-hash-user args))

(defn password-mine-reset [context args _]
  (println "args password-mine-reset" args)
  (Thread/sleep 1000) ; wait to take tome for brute force attack
  (let [user (get-user-loggedin context)]
    (when-not (empty? user)
      (model.user/reset-password-with-checking-current-password (:id user) args))))

(defn device-create [context args _]
  (println "args device-create" args)
  (let [user (get-user-loggedin context)
        params_device_type (:device args)]
    (when (model.user/admin? user)
      (model.device/create-for-user params_device_type (:id user)))))

(defn device [context args _]
  (println "args device" args)
  (let [user (get-user-loggedin context)]
    (model.device/get-by-id-for-user (:id args) (:id user))))

(defn device-update [context args _]
  (println "args device-update" args)
  (let [user (get-user-loggedin context)
        params (-> args :device)]
    (when (model.user/admin? user)
      (model.device/for-user-update {:id (:id args) :id-user (:id user) :params params}))))

(defn device-delete [context args _]
  (println "args device-delete" args)
  (let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device/for-user-delete {:id (:id args) :id-user (:id user)}))))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/raw_device_logs_for_device raw-device-logs-for-device
   :Query/raw_device_logs_for_device_type raw-device-logs-for-device-type
   :Query/raw_device_logs_for_device_watch_group raw-device-logs-for-device-watch-group
   :Query/device_type_api_keys_for_device_type device-type-api-keys-for-device-type
   :Query/device_type_api_key_for_device_type device-type-api-key-for-device-type
   :Query/device_watch_groups device-watch-groups
   :Query/device_watch_group device-watch-group
   :Query/device_watch_group_devices_for_device_watch_group device-watch-group-devices-for-device-watch-group
   :Query/device_watch_group_device_for_device_watch_group device-watch-group-device-for-device-watch-group
   :Query/device_files_for_device device-files-for-device
   :Query/users users
   :Query/user user
   :Query/user_teams user-teams
   :Query/user_team user-team
   :Query/user_for_resetting_password user-for-resetting-password
   :Query/devices devices
   :Query/device device
   :Query/device_types device-types
   :Query/device_type device-type
   :Query/user_loggedin user-loggedin
   :Mutation/user_create user-create
   :Mutation/user_update user-update
   :Mutation/user_delete user-delete
   :Mutation/user_team_create user-team-create
   :Mutation/user_team_update user-team-update
   :Mutation/user_team_delete user-team-delete
   :Mutation/device_create device-create
   :Mutation/device_update device-update
   :Mutation/device_delete device-delete
   :Mutation/device_type_create device-type-create
   :Mutation/device_type_update device-type-update
   :Mutation/device_type_delete device-type-delete
   :Mutation/device_type_api_key_create device-type-api-key-create
   :Mutation/device_type_api_key_update device-type-api-key-update
   :Mutation/device_type_api_key_delete device-type-api-key-delete
   :Mutation/device_watch_group_create device-watch-group-create
   :Mutation/device_watch_group_update device-watch-group-update
   :Mutation/device_watch_group_delete device-watch-group-delete
   :Mutation/device_watch_group_device_create device-watch-group-device-create
   :Mutation/device_watch_group_device_update device-watch-group-device-update
   :Mutation/device_watch_group_device_delete device-watch-group-device-delete
   :Mutation/hash_for_resetting_password_create hash-for-resetting-password-create
   :Mutation/password_for_hash_user_reset password-for-hash-user-reset
   :Mutation/password_mine_reset password-mine-reset
   :Mutation/login login
   :Mutation/logout logout})
