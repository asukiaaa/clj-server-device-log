(ns asuki.back.graphql.resolver
  (:require [asuki.back.models.raw-device-log :as model-raw-device-log]))

(defn raw-device-logs
  [context args _]
  (println "args in raw-device-logs" args)
  (let [records-and-total (model-raw-device-log/get-records-with-total args)
        logs (:records records-and-total)
        total (:total records-and-total)]
    {:list logs
     :total total}))

(def resolver-map
  {:Query/raw_device_logs raw-device-logs})
