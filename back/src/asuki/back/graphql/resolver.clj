(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]
            [asuki.back.models.user :as model.user]
            [com.walmartlabs.lacinia.resolve :as resolve]))

(defn raw-device-logs
  [_ args _]
  (println "args for raw-device-logs" args)
  (let [records-and-total (model-raw-device-log/get-records-with-total args)
        logs (:records records-and-total)
        total (:total records-and-total)]
    {:list logs
     :total total}))

(defn login [context args _]
  (println "args for login" args)
  (let [; error-login (:error-login) ; TODO
        user-loggedin-now (:user-loggedin-now context)]
    user-loggedin-now))

(defn logout [_ _ _] true)

(defn user-loggedin [context args _]
  (println "args for user-loggedin" args)
  (let [context-user-loggedin (:user-loggedin context)]
    #_(println context-user-loggedin)
    (when-let [id (:id context-user-loggedin)]
      (model.user/get-by-id id))))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Query/user_loggedin user-loggedin
   :Mutation/login login
   :Mutation/logout logout})
