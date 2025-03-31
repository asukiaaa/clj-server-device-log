(ns front.view.devices.device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-log :as model.device-log]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.devices.util :as v.device.util]
            [front.view.util.device-log.page :as device-log.page]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        [device set-device] (react/useState)
        on-receive #(set-device (:device %))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/devices) :path route/devices}
       {:label (util.label/device-item device) :path (route/device-show id-device)}
       {:label util.label/logs}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links device))
     (device-log.page/core
      #(model.device-log/fetch-list-and-total-for-device
        (assoc % :id-device id-device))
      {:map-default {:str-where "[]"}
       :on-receive on-receive})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
