(ns back.graphql.resolver
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as jdbc]
            [back.config :refer [db-spec]]
            [back.models.device-log :as model-device-log]
            [back.models.user :as model.user]
            [back.models.user-team :as model.user-team]
            [back.models.user-team-device-config :as model.user-team-device-config]
            [back.models.user-team-device-type-config :as model.user-team-device-type-config]
            [back.models.user-team-member :as model.user-team-member]
            [back.models.device :as model.device]
            [back.models.device-type :as model.device-type]
            [back.models.device-type-api-key :as model.device-type-api-key]
            [back.models.watch-scope :as model.watch-scope]
            [back.models.watch-scope-term :as model.watch-scope-term]
            [back.models.device-file :as model.device-file]
            [com.walmartlabs.lacinia.resolve :refer [resolve-as]]
            [back.models.util.device :as util.device]
            [back.models.util.device-permission :as util.device-permission]
            [back.models.util.user :as util.user]
            [back.models.util.user-permission :as util.user-permission]
            [back.models.util.user-team-permission :as util.user-team-permission]
            [back.models.util.watch-scope :as util.watch-scope]
            [back.util.label :as util.label]))

(defn get-user-loggedin [context]
  (:user-loggedin context))

(defn handle-only-for-admin [context fn-to-handle]
  (if (model.user/admin? (get-user-loggedin context))
    (fn-to-handle)
    (resolve-as nil {:message "no permission to handle"})))

(defn device-logs
  [_ args _]
  (println "args for device-logs" args)
  (model-device-log/get-list-with-total args {:build-str-where-and #(format "%s.device_id IS NULL" %)}))

(defn device-logs-for-device
  [context args _]
  (println "args for device-logs-for-device" args)
  (when-let [id-device (:device_id args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [user (get-user-loggedin context)]
        (when-let [device (model.device/get-by-id-for-user id-device (:id user) {:transaction transaction})]
          (-> (model-device-log/get-list-with-total
               args
               {:build-str-where-and #(format "%s.device_id = %d" % id-device)
                :transaction transaction})
              (assoc :device device)))))))

(defn device-logs-for-device-type
  [context args _]
  (println "args for device-logs-for-device-type" args)
  (when-let [user (get-user-loggedin context)]
    (let [id-device-type (:device_type_id args)
          id-user (:id user)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when-let [device-type (model.device-type/get-by-id-for-user-via
                                id-device-type id-user {:via-device true
                                                        :via-manager true
                                                        :transaction transaction})]
          (-> (model-device-log/get-list-with-total
               args {:build-str-where-and
                     (fn [_]
                       (format "%s.id IN %s"
                               util.device/name-table
                               (-> id-user
                                   util.user-team-permission/build-query-ids-for-user-show
                                   (util.device-permission/build-query-ids-for-user-teams-via
                                    {:via-device true :via-manager true}))))
                     :transaction transaction})
              (assoc model.device-type/key-table device-type)))))))

(defn device-logs-for-watch-scope
  [context args _]
  (println "args for device-logs-for-watch-scope" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-watch-scope (:watch_scope_id args)
          watch-scope (model.watch-scope/get-by-id id-watch-scope {:transaction transaction})]
      (when (or (model.user/admin? user)
                (model.user-team/user-has-permission-to-read {:id-user-team (:user_team_id watch-scope)
                                                              :id-user (:id user)}))
        (model-device-log/get-list-with-total
         args
         {:build-str-where-and
          (fn [name-table-device-log]
            (format "%s.id IN %s"
                    name-table-device-log
                    (model.watch-scope-term/build-sql-ids-device-log-for-watch-scope id-watch-scope)))})))))

(defn login [context args _]
  (println "requested user login")
  #_(println "args for login" args)
  (get-user-loggedin context))

(defn logout [_ _ _]
  (println "received logout request")
  true)

(defn user-loggedin [context args _]
  (println "args for user-loggedin" args)
  (if-let [user-loggedin (get-user-loggedin context)]
    {model.user/key-table user-loggedin}
    {:errors (json/write-str ["not found"])}))

(defn users [context args _]
  (println "args for users" args)
  (when-let [user (get-user-loggedin context)]
    (if (model.user/admin? user)
      (model.user/get-list-with-total-for-admin args)
      (let [sql-ids-user
            (-> (util.user-team-permission/build-query-ids-for-user-show (:id user))
                util.user-permission/build-query-ids-for-user-teams)]
        (model.user/get-list-with-total-by-ids args sql-ids-user)))))

(defn user [context args _]
  (println "args for user" args)
  (when-let [user (get-user-loggedin context)]
    (when-let [id-user (:id args)]
      (if (model.user/admin? user)
        (model.user/get-by-id-with-permission id-user)
        (let [sql-ids-user
              (-> (util.user-team-permission/build-query-ids-for-user-show (:id user))
                  util.user-permission/build-query-ids-for-user-teams)]
          (model.user/get-by-id-in-ids id-user sql-ids-user))))))

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

(defn profile-update [context args _]
  (when-let [user (get-user-loggedin context)]
    (let [params-user (util.user/key-table args)]
      (model.user/update (:id user)
                         (if (model.user/admin? user)
                           params-user
                           (select-keys params-user util.user/keys-param))))))

(defn user-teams
  [context args _]
  (println "args for user-teams" args)
  (when-let [user (get-user-loggedin context)]
    (if (model.user/admin? user)
      (model.user-team/get-list-with-total args)
      (model.user-team/get-list-with-total-for-ids args (util.user-team-permission/build-query-ids-for-user-show (:id user))))))

(defn user-teams-for-device-type
  [context args _]
  (println "args for user-teams-for-device-type" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [id-device-type (:device_type_id args)
            is-admin (model.user/admin? user)
            device-type (if is-admin
                          (model.device-type/get-by-id id-device-type {:transaction transaction})
                          (model.device-type/get-by-id-for-user-via id-device-type (:id user)
                                                                    {:via-device true :via-manager true
                                                                     :transaction transaction}))
            sql-ids-user-team (when-not is-admin
                                (util.user-team-permission/build-query-ids-for-user-write (:id user)))
            sql-ids-user-team (util.device/build-sql-ids-user-team-for-device-type
                               id-device-type {:sql-ids-user-team sql-ids-user-team})
            list-and-total (when device-type
                             (model.user-team/get-list-with-total-for-ids args sql-ids-user-team {:transaction transaction}))]
        (assoc list-and-total model.device-type/key-table device-type)))))

(defn user-team
  [context args _]
  (println "args for user-team" args)
  (when-let [user (get-user-loggedin context)]
    (if (model.user/admin? user)
      (model.user-team/get-by-id (:id args))
      (model.user-team/get-by-id-for-user (:id args) (:id user)))))

(defn user-team-create [context args _]
  (println "args user-team-create" args)
  (let [user (get-user-loggedin context)
        params (model.user-team/key-table args)]
    (when (model.user/admin? user) (model.user-team/create params))))

(defn user-team-update [context args _]
  (println "args user-team-update" args)
  (let [user (get-user-loggedin context)
        id (:id args)
        params (model.user-team/key-table args)]
    (when (model.user/admin? user) (model.user-team/update id params))))

(defn user-team-delete [context args _]
  (println "args user-team-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (when (model.user/admin? user) (model.user-team/delete id))))

(defn user-team-device-type-configs-for-device-type
  [context args _]
  (println "args for user-team-device-type-configs-for-device-type" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [id-device-type (:device_type_id args)
            device-type (if (model.user/admin? user)
                          (model.device-type/get-by-id id-device-type {:transaction transaction})
                          (model.device-type/get-by-id-for-user-via
                           id-device-type (:id user) {:via-manager true
                                                      :via-device true
                                                      :transaction transaction}))]
        (when device-type
          (-> (model.user-team-device-type-config/get-list-with-total-for-device-type args id-device-type {:transaction transaction})
              (assoc model.device-type/key-table device-type)))))))

(defn user-team-device-type-config
  [context args _]
  (println "args for user-team-device-type-config" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [id-user-team (:user_team_id args)
            id-device-type (:device_type_id args)]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-read
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-device-type-config/get-by-user-team-and-device-type
           id-user-team id-device-type {:transaction transaction}))))))

(defn user-team-device-type-config-to-edit
  [context args _]
  (println "args for user-team-device-type-config-to-edit" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [id-user-team (:user_team_id args)
            id-device-type (:device_type_id args)]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (if-let [config (model.user-team-device-type-config/get-by-user-team-and-device-type
                           id-user-team id-device-type {:transaction transaction})]
            config
            {:device_type_id id-device-type
             model.device-type/key-table
             (model.device-type/get-by-id id-device-type (:transaction transaction))
             :user_team_id id-user-team
             model.user-team/key-table
             (model.user-team/get-by-id id-user-team {:transaction transaction})}))))))

(defn user-team-device-type-config-update [context args _]
  (println "args user-team-device-type-config-update" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [params (model.user-team-device-type-config/key-table args)
            id-user-team (:user_team_id args)
            id-device-type (:device_type_id args)]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-device-type-config/update id-user-team id-device-type params {:transaction transaction})
          {model.user-team-device-type-config/key-table
           (model.user-team-device-type-config/get-by-user-team-and-device-type id-user-team id-device-type {:transaction transaction})})))))

(defn user-team-device-type-config-delete [context args _]
  (println "args user-team-device-type-config-delete" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [id-user-team (:user_team_id args)
            id-device-type (:device_type_id args)]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-device-type-config/delete id-user-team id-device-type {:transaction transaction}))))))

(defn user-team-members-for-user-team
  [context args _]
  (println "args for user-team-members-for-user-team" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (when-let [user (get-user-loggedin context)]
      (let [id-user-team (:user_team_id args)
            user-team (if (model.user/admin? user)
                        (model.user-team/get-by-id id-user-team {:transaction transaction})
                        (model.user-team/get-by-id-for-user id-user-team (:id user) {:transaction transaction}))]
        (when user-team
          (-> (model.user-team-member/get-list-with-total-for-user-team args id-user-team {:transaction transaction})
              (assoc model.user-team/key-table user-team)))))))

(defn user-team-member-for-user-team
  [context args _]
  (println "args for user-team-member-for-user-team" args)
  (when-let [user (get-user-loggedin context)]
    (let [id-user-team (:user_team_id args)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-read
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-member/get-by-id (:id args)))))))

(defn user-team-member-create [context args _]
  (println "args user-team-member-create" args)
  (when-let [user (get-user-loggedin context)]
    (let [params (model.user-team-member/key-table args)
          id-user-team (:user_team_id params)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team id-user-team
                    :id-user (:id user)
                    :transaction transaction}))
          (if-let [member (model.user/get-by-email (:user_email params))]
            (model.user-team-member/create (assoc params :member_id (:id member)) {:transaction transaction})
            (:errors (json/write-str {:user_email ["Not found"]}))))))))

(defn user-team-member-update [context args _]
  (println "args user-team-member-update" args)
  (when-let [user (get-user-loggedin context)]
    (let [id (:id args)
          params (model.user-team-member/key-table args)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team (model.user-team-member/build-query-id-user-team id)
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-member/update id params {:transaction transaction}))))))

(defn user-team-member-delete [context args _]
  (println "args user-team-member-delete" args)
  (when-let [user (get-user-loggedin context)]
    (let [id (:id args)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when (or (model.user/admin? user)
                  (model.user-team/user-has-permission-to-write
                   {:id-user-team (model.user-team-member/build-query-id-user-team id)
                    :id-user (:id user)
                    :transaction transaction}))
          (model.user-team-member/delete id {:transaction transaction}))))))

(defn devices [context args _]
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [list-and-total
            (if (model.user/admin? user)
              (model.device/get-list-with-total-for-admin args)
              (let [sql-ids-user-team (util.user-team-permission/build-query-ids-for-user-show (:id user))]
                (model.device/get-list-with-total-for-user-teams-via
                 args sql-ids-user-team
                 {:via-device true :via-manager true
                  :transaction transaction})))
            list-with-terms (model.watch-scope-term/assign-actives-to-list-device (:list list-and-total) {:transaction transaction})]
        (assoc list-and-total
               :list list-with-terms)))))

(defn devices-for-user-team [context args _]
  (println "args devices-for-user-team" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-user-team (:user_team_id args)
          user-team (if (model.user/admin? user)
                      (model.user-team/get-by-id id-user-team {:transaction transaction})
                      (model.user-team/get-by-id-for-user id-user-team (:id user) {:transaction transaction}))
          list-and-total (when user-team (model.device/get-list-with-total-for-user-team-via
                                          args id-user-team {:via-device true
                                                             :transaction transaction}))]
      (assoc list-and-total model.user-team/key-table user-team))))

(defn device-types [context args _]
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (if (model.user/admin? user)
        (model.device-type/get-list-with-total args {:transaction transaction})
        (model.device-type/get-list-with-total-for-user-via
         args (:id user)
         {:via-device true
          :via-manager true
          :transaction transaction})))))

(defn device-types-for-user-team [context args _]
  (println "args device-types-for-user-team" args)
  (let [user (get-user-loggedin context)
        id-user (:id user)
        id-user-team (:user_team_id args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (when-let [user-team (when
                            (or (model.user/admin? user)
                                (model.user-team/user-has-permission-to-read {:id-user id-user :id-user-team id-user-team :transaction transaction}))
                             (model.user-team/get-by-id id-user-team {:transaction transaction}))]
        (-> (model.device-type/get-list-with-total-for-user-team-via
             args id-user-team {:via-device true
                                :transaction transaction})
            (assoc model.user-team/key-table user-team))))))

(defn device-type [context args _]
  (when-let [user (get-user-loggedin context)]
    (if (model.user/admin? user)
      (model.device-type/get-by-id (:id args) (:id user))
      (model.device-type/get-by-id-for-user-via (:id args) (:id user) {:via-device true :via-manager true}))))

(defn device-type-create [context args _]
  (println "args device-type-create" args)
  (let [user (get-user-loggedin context)
        params-device-type (:device_type args)]
    (when (model.user/admin? user)
      (model.device-type/create params-device-type))))

(defn device-type-update [context args _]
  (println "args device-type-create" args)
  (when-let [user (get-user-loggedin context)]
    (let [id-device-type (:id args)
          id-user (:id user)
          params-device-type (:device_type args)]
      (model.device-type/update-for-user {:id id-device-type
                                          :id-user id-user
                                          :params params-device-type}))))

(defn device-type-delete [context args _]
  (println "args device-type-delete" args)
  (when-let [user (get-user-loggedin context)]
    (model.device-type/delete-for-user {:id (:id args)
                                        :id-user (:id user)})))

(defn device-type-api-keys-for-device-type [context args _]
  (println "args for device-type-api-keys-for-device-type" args)
  (when-let [user (get-user-loggedin context)]
    (let [id-user (:id user)
          id-device-type (:device_type_id args)]
      (jdbc/with-db-transaction [transaction db-spec]
        (when-let [device-type (model.device-type/get-by-id-for-user-to-edit
                                id-device-type id-user {:transaction transaction})]
          (-> (model.device-type-api-key/get-list-with-total-for-device-type
               args id-device-type {:transaction transaction})
              (assoc model.device-type/key-table device-type)))))))

(defn device-type-api-key-for-device-type [context args _]
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

(defn device-files-for-device [context args _]
  (println "args for device-files-for-device" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-device (:device_id args)
          device (model.device/get-by-id-for-user id-device (:id user) {:transaction transaction})
          files-list-total
          (when device
            (model.device-file/get-list-with-total-for-device args id-device {:transaction transaction}))]
      (assoc files-list-total model.device/key-table device))))

(defn device-files-latest-each-device [context args _]
  (println "args for device-files-latest-each-device" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [sql-ids-user-team (util.user-team-permission/build-query-ids-for-user-show (:id user))
            sql-ids-devices (util.device/build-sql-ids-for-user-teams sql-ids-user-team)
            list-and-total (if (model.user/admin? user)
                             (model.device-file/get-list-with-total-latest-each-device-for-admin args {:transaction transaction})
                             (model.device-file/get-list-with-total-latest-each-device args sql-ids-devices {:transaction transaction}))]
        list-and-total))))

(defn device-files-for-watch-scope [context args _]
  (println "args for device-files-for-watch-scope" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-watch-scope (:watch_scope_id args)
          watch-scope
          (if (model.user/admin? user)
            (model.watch-scope/get-by-id id-watch-scope {:transaction transaction})
            (model.watch-scope/get-by-id-for-user-teams
             id-watch-scope (util.user-team-permission/build-query-ids-for-user-show (:id user))
             {:transaction transaction}))
          sql-ids-device-file (model.watch-scope-term/build-sql-ids-device-file-for-watch-scope id-watch-scope)
          files-list-total
          (when watch-scope
            (model.device-file/get-list-with-total-for-ids args sql-ids-device-file {:transaction transaction}))]
      (assoc files-list-total model.watch-scope/key-table watch-scope))))

(defn watch-scopes [context args _]
  (println "args for watch-scopes" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [list-and-total
            (cond
              (model.user/admin? user)
              (model.watch-scope/get-list-with-total args)
              :else
              (let [sql-ids-user-team (util.user-team-permission/build-query-ids-for-user-show (:id user))]
                (model.watch-scope/get-list-with-total-for-user-teams args sql-ids-user-team)))
            list-watch-scope (-> (:list list-and-total)
                                 (model.watch-scope-term/assign-to-list-watch-scope {:transaction transaction}))]
        (assoc list-and-total :list list-watch-scope)))))

(defn watch-scope [context args _]
  (println "args for watch-scope" args)
  (when-let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (when-let [watch-scope
                 (if (model.user/admin? user)
                   (model.watch-scope/get-by-id (:id args) {:transaction transaction})
                   (model.watch-scope/get-by-id-for-user-teams
                    (:id args)
                    (util.user-team-permission/build-query-ids-for-user-show (:id user))
                    {:transaction transaction}))]
        (let [terms (model.watch-scope-term/get-list-for-watch-scope (:id watch-scope) {:transaction transaction})]
          (assoc watch-scope :terms terms))))))

(defn watch-scope-create [context args _]
  (println "args watch-scope-create" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          params (:watch_scope args)
          id-user-team (:user_team_id params)
          user-team (when (or (model.user/admin? user)
                              (model.user-team/user-has-permission-to-write
                               {:id-user (:id user)
                                :id-user-team id-user-team
                                :transaction transaction}))
                      (model.user-team/get-by-id id-user-team {:transaction transaction}))]
      (if user-team
        (let [watch-scope (model.watch-scope/create params {:transaction transaction})]
          (model.watch-scope-term/create-list-for-watch-scope (:id watch-scope) (:terms params) {:transaction transaction})
          {model.watch-scope/key-table watch-scope})
        {:errors (json/write-str [(util.label/no-permission)])}))))

(defn watch-scope-update [context args _]
  (println "args watch-scope-update" args)
  (jdbc/with-db-transaction [transaction db-spec]
    (let [user (get-user-loggedin context)
          id-watch-scope (:id args)
          params-watch-scope (:watch_scope args)
          params-terms (:terms params-watch-scope)]
      (if (or (model.user/admin? user)
              (model.user-team/user-has-permission-to-write
               {:id-user (:id user)
                :id-user-team (util.watch-scope/build-query-get-id-user-team id-watch-scope)
                :transaction transaction}))
        (when-let [watch-scope (model.watch-scope/update id-watch-scope params-watch-scope {:transaction transaction})]
          (model.watch-scope-term/delete-list-for-watch-scope id-watch-scope {:transaction transaction})
          (model.watch-scope-term/create-list-for-watch-scope id-watch-scope params-terms {:transaction transaction})
          (assoc watch-scope :terms (model.watch-scope-term/get-list-for-watch-scope id-watch-scope)))
        {:errors (json/write-str [(util.label/no-permission)])}))))

(defn watch-scope-delete [context args _]
  (println "args watch-scope-delete" args)
  (let [user (get-user-loggedin context)
        id (:id args)]
    (if (or (model.user/admin? user)
            (model.user-team/user-has-permission-to-write
             {:id-user (:id user)
              :id-user-team (util.watch-scope/build-query-get-id-user-team id)}))
      (model.watch-scope/delete id)
      {:errors (json/write-str [(util.label/no-permission)])})))

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

(defn device [context args _]
  (println "args device" args)
  (let [user (get-user-loggedin context)]
    (jdbc/with-db-transaction [transaction db-spec]
      (-> (model.device/get-by-id-for-user (:id args) (:id user) {:transaction transaction})
          (model.watch-scope-term/assign-actives-to-device {:transaction transaction})))))

(defn create-user-team-device-config-with-checking-permission [{:keys [is-admin params id-user id-device transaction]}]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (let [id-user-team (:user_team_id params)]
      (when (or is-admin
                (model.user-team/user-has-permission-to-write
                 {:id-user id-user
                  :id-user-team id-user-team
                  :transaction transaction}))
        (model.user-team-device-config/delete-and-create-for-device
         params id-device {:transaction transaction})))))

(defn device-create [context args _]
  (println "args device-create" args)
  (let [user (get-user-loggedin context)
        id-user (:id user)
        is-admin (model.user/admin? user)
        params-device (model.device/key-table args)
        params-config (model.user-team-device-config/key-table args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [device
            (when is-admin
              (model.device/create-for-user params-device id-user {:transaction transaction}))
            user-team-device-config
            (create-user-team-device-config-with-checking-permission
             {:is-admin is-admin
              :params params-config
              :id-user id-user
              :id-device (:id device)
              :transaction transaction})]
        {model.device/key-table device
         model.user-team-device-config/key-table user-team-device-config}))))

(defn device-update [context args _]
  (println "args device-update" args)
  (let [user (get-user-loggedin context)
        id-user (:id user)
        is-admin (model.user/admin? user)
        id-device (:id args)
        params-device (model.device/key-table args)
        params-config (model.user-team-device-config/key-table args)]
    (jdbc/with-db-transaction [transaction db-spec]
      (let [device
            (if is-admin
              (model.device/update-for-admin id-device params-device {:transaction transaction})
              (model.device/update-for-user {:id id-device :id-user id-user :params params-device :transaction transaction}))
            user-team-device-config
            (create-user-team-device-config-with-checking-permission
             {:is-admin is-admin
              :params params-config
              :id-user id-user
              :id-device (:id device)
              :transaction transaction})]
        {model.device/key-table device
         model.user-team-device-config/key-table user-team-device-config}))))

(defn device-delete [context args _]
  (println "args device-delete" args)
  (let [user (get-user-loggedin context)]
    (when (model.user/admin? user)
      (model.device/delete-for-user (:id args) (:id user)))))

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
   :Query/device_files_for_watch_scope device-files-for-watch-scope
   :Query/user user
   :Query/users users
   :Query/user_team user-team
   :Query/user_teams user-teams
   :Query/user_teams_for_device_type user-teams-for-device-type
   :Query/user_team_device_type_config user-team-device-type-config
   :Query/user_team_device_type_config_to_edit user-team-device-type-config-to-edit
   :Query/user_team_device_type_configs_for_device_type user-team-device-type-configs-for-device-type
   :Query/user_team_members_for_user_team user-team-members-for-user-team
   :Query/user_team_member_for_user_team user-team-member-for-user-team
   :Query/user_for_resetting_password user-for-resetting-password
   :Query/device device
   :Query/devices devices
   :Query/devices_for_user_team devices-for-user-team
   :Query/device_type device-type
   :Query/device_types device-types
   :Query/device_types_for_user_team device-types-for-user-team
   :Query/user_loggedin user-loggedin
   :Mutation/user_create user-create
   :Mutation/user_update user-update
   :Mutation/user_delete user-delete
   :Mutation/user_team_create user-team-create
   :Mutation/user_team_update user-team-update
   :Mutation/user_team_delete user-team-delete
   :Mutation/user_team_device_type_config_update user-team-device-type-config-update
   :Mutation/user_team_device_type_config_delete user-team-device-type-config-delete
   :Mutation/user_team_member_create user-team-member-create
   :Mutation/user_team_member_update user-team-member-update
   :Mutation/user_team_member_delete user-team-member-delete
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
   :Mutation/profile_update profile-update
   :Mutation/login login
   :Mutation/logout logout})
