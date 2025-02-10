(ns back.graphql.resolver
  (:require [clojure.java.jdbc :as jdbc]
            [back.config :refer [db-spec]]
            [back.models.device-log :as model-device-log]
            [back.models.user :as model.user]
            [back.models.user-team :as model.user-team]
            [back.models.device :as model.device]
            [back.models.device-type :as model.device-type]
            [back.models.device-type-api-key :as model.device-type-api-key]
            [back.models.watch-scope :as model.watch-scope]
            [back.models.watch-scope-term :as model.watch-scope-term]
            [back.models.device-file :as model.device-file]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]))

(defn get-user-loggedin [context]
  (:user-loggedin context))

(defn handle-only-for-admin [context fn-to-handle]
  (if (model.user/admin? (get-user-loggedin context))
    (fn-to-handle)
    (resolve-as nil {:message "no permission to handle"})))

(defn device-logs
  [_ args _]
  (println "args for device-logs" args)
  (model-device-log/get-list-with-total args {:str-where-and "device_id IS NULL"}))

(defn device-logs-for-device
  [context args _]
  (println "args for device-logs-for-device" args)
  (when-let [id-device (:device_id args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [user (get-user-loggedin context)]
        (when-let [device (model.device/get-by-id-for-user id-device (:id user) {:transaction transaction})]
          (-> (model-device-log/get-list-with-total
               args
               {:str-where-and (format "device_id = %d" id-device)
                :transaction transaction})
              (assoc :device device)))))))

(defn device-logs-for-device-type
  [context args _]
  (println "args for device-logs-for-device-type" args)
  (let [id-device-type (:device_type_id args)
        user (get-user-loggedin context)
        id-user (:id user)]
    (jdbc/with-db-transaction [transaction db-spec]
      (when-let [device-type (model.device-type/get-by-id-for-user id-device-type id-user {:transaction transaction})]
        (-> (model-device-log/get-list-with-total args {:str-where-and (format "device_type.id = %d" id-device-type)
                                                        :transaction transaction})
            (assoc model.device-type/key-table device-type))))))

(defn device-logs-for-watch-scope
  [context args _]
  (println "args for device-logs-for-watch-scope" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (when-let [id-watch-scope (:watch_scope_id args)]
        (let [query-device-ids (model.watch-scope-term/build-query-device-ids-for-watch-scope id-watch-scope)]
          (model-device-log/get-list-with-total args {:str-where-and (format "device_id IN %s" query-device-ids)}))))))

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
      (model.user-team/get-list-with-total args))))

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

(defn devices-for-user-team [context args _]
  (println "args devices-for-user-team" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-user-team (:user_team_id args)
          user-team (model.user-team/get-by-id-for-owner-user id-user-team (:id user) {:transaction transaction})
          list-and-total (when user-team (model.device/get-list-with-total-for-user-team args id-user-team {:transaction transaction}))]
      list-and-total)))

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

(defn device-files-latest-each-device
  [context args _]
  (println "args for device-files-latest-each-device" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          sql-ids-user-team (model.user-team/build-sql-ids-for-user (:id user))
          sql-ids-devices (model.device/build-sql-ids-for-user-teams sql-ids-user-team)
          list-and-total (model.device-file/get-list-with-total-latest-each-device args sql-ids-devices {:transaction transaction})]
      list-and-total)))

(defn watch-scopes
  [context args _]
  (println "args for watch-scopes" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.watch-scope/get-list-with-total args))))

(defn watch-scope
  [context args _]
  (println "args for watch-scope" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (jdbc/with-db-transaction [transaction db-spec]
        (when-let [watch-scope (model.watch-scope/get-by-id (:id args) {:transaction transaction})]
          (let [terms (model.watch-scope-term/get-list-for-watch-scope (:id watch-scope) {:transaction transaction})
                devices (model.device/get-list-by-ids (map :device_id terms) {:transaction transaction})
                map-device (into {} (for [device devices] [(:id device) device]))
                terms (for [term terms]
                        (assoc term :device (get map-device (:device_id term))))]
            (assoc watch-scope :terms terms)))))))

(defn watch-scope-create [context args _]
  (println "args watch-scope-create" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          params (:watch_scope args)
          id-user-team (:user_team_id params)
          user-team (if (model.user/admin? user)
                      (model.user-team/get-by-id id-user-team {:transaction transaction})
                      (model.user-team/get-by-id-for-owner-user id-user-team (:id user) {:transaction transaction}))]
      (when user-team
        (let [watch-scope (model.watch-scope/create params {:transaction transaction})]
          (model.watch-scope-term/create-list-for-watch-scope (:id watch-scope) (:terms params) {:transaction transaction})
          {model.watch-scope/key-table watch-scope})))))

(defn watch-scope-update [context args _]
  (println "args watch-scope-update" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-watch-scope (:id args)
          params-watch-scope (:watch_scope args)
          params-terms (:terms params-watch-scope)]
      (when (model.user/admin? user)
        (when-let [watch-scope (model.watch-scope/update id-watch-scope params-watch-scope {:transaction transaction})]
          (model.watch-scope-term/delete-list-for-watch-scope id-watch-scope {:transaction transaction})
          (model.watch-scope-term/create-list-for-watch-scope id-watch-scope params-terms {:transaction transaction})
          (assoc watch-scope :terms (model.watch-scope-term/get-list-for-watch-scope id-watch-scope)))))))

(defn watch-scope-delete [context args _]
  (println "args watch-scope-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.watch-scope/delete id))))

(defn watch-scope-terms-for-watch-scope
  [context args _]
  (println "args for watch-scope-terms" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (jdbc/with-db-transaction [transaction db-spec]
        (when-let [id-watch-scope (:watch_scope_id args)]
          (when-let [watch-scope (model.watch-scope/get-by-id id-watch-scope {:transaction transaction})]
            (-> (model.watch-scope-term/get-list-with-total args {:transaction transaction})
                (assoc model.watch-scope/key-table watch-scope))))))))

(defn watch-scope-term-for-watch-scope
  [context args _]
  (println "args for watch-scope-term" args)
  (when-let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.watch-scope-term/get-by-id (:id args)))))

(defn watch-scope-term-create [context args _]
  (println "args watch-scope-term-create" args)
  (let [user (get-user-loggedin context)
        params (:watch_scope_term args)]
    (when (model.user/admin? user) (model.watch-scope-term/create params))))

(defn watch-scope-term-update [context args _]
  (println "args watch-scope-term-update" args)
  (let [user (get-user-loggedin context)
        id (:id args)
        params (:watch_scope_term args)]
    (when (model.user/admin? user) (model.watch-scope-term/update id params))))

(defn watch-scope-term-delete [context args _]
  (println "args watch-scope-term-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.watch-scope-term/delete id))))

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

(defn authorization-bearer-for-device [context args _]
  (println "authorization-bearer-for-device")
  (let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (let [bearer (model.device/get-authorizaton-bearer-by-id (:id args))]
        {:authorization_bearer bearer}))))

(defn authorization-bearer-for-device-type-api-key [context args _]
  (println "authorization-bearer-for-device-type-api-key")
  (let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (let [bearer (model.device-type-api-key/get-authorizaton-bearer-by-id (:id args))]
        {:authorization_bearer bearer}))))

(def resolver-map
  {:Query/authorization_bearer_for_device authorization-bearer-for-device
   :Query/authorization_bearer_for_device_type_api_key authorization-bearer-for-device-type-api-key
   :Query/device_logs device-logs
   :Query/device_logs_for_device device-logs-for-device
   :Query/device_logs_for_device_type device-logs-for-device-type
   :Query/device_logs_for_watch_scope device-logs-for-watch-scope
   :Query/device_type_api_keys_for_device_type device-type-api-keys-for-device-type
   :Query/device_type_api_key_for_device_type device-type-api-key-for-device-type
   :Query/watch_scopes watch-scopes
   :Query/watch_scope watch-scope
   :Query/watch_scope_terms_for_watch_scope watch-scope-terms-for-watch-scope
   :Query/watch_scope_term_for_watch_scope watch-scope-term-for-watch-scope
   :Query/device_files_for_device device-files-for-device
   :Query/device_files_latest_each_device device-files-latest-each-device
   :Query/users users
   :Query/user user
   :Query/user_teams user-teams
   :Query/user_team user-team
   :Query/user_for_resetting_password user-for-resetting-password
   :Query/device device
   :Query/devices devices
   :Query/devices_for_user_team devices-for-user-team
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
   :Mutation/watch_scope_create watch-scope-create
   :Mutation/watch_scope_update watch-scope-update
   :Mutation/watch_scope_delete watch-scope-delete
   :Mutation/watch_scope_term_create watch-scope-term-create
   :Mutation/watch_scope_term_update watch-scope-term-update
   :Mutation/watch_scope_term_delete watch-scope-term-delete
   :Mutation/hash_for_resetting_password_create hash-for-resetting-password-create
   :Mutation/password_for_hash_user_reset password-for-hash-user-reset
   :Mutation/password_mine_reset password-mine-reset
   :Mutation/login login
   :Mutation/logout logout})
