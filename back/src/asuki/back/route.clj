(ns asuki.back.route
  (:require [asuki.back.handlers.core :as handlers]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http :refer [html-body]]
            [asuki.back.handlers.graphql :as handler-graphql]))

(def main
  #{["/" :get [html-body handlers/top] :route-name :top]
    ["/device_logs" :get [html-body handlers/device-logs] :route-name :device-logs]
    ["/device_logs/:id" :get [html-body handlers/device-log]
     :route-name :show-device-log
     :constraints  {:id #"[0-9]+"}]
    ["/graphql" :post [(body-params) handler-graphql/core] :route-name :graphql]
    ["/api/raw_device_log"
     :post [(body-params) handlers/api-raw-device-log]
     :route-name :post-raw-device-log]
    ["/404" :get [html-body handlers/handle-404] :route-name :show-404]})
