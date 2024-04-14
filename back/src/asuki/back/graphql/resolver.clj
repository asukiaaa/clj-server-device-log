(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]
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
  #_(println :body (:body (:request context)))
  #_(println :session (:session (:request context)))
  (let [user (:loggedin-user context)]
    (println user)
    (println (keys context))
    #_(println :user-info (:user-info context))
    #_(println :session (:session (:request context)))
    user))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs
   :Mutation/login login})
