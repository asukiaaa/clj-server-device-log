(ns front.view.devices.device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-log :as model.device-log]
            [front.model.util.device :as m.util.device]
            [front.model.util.device-type :as m.util.device-type]
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
        on-receive
        (fn [data _errors {:keys [info-str-renderer]}]
          (let [device (m.util.device/key-table data)
                device-type (m.util.device-type/key-table device)
                config-renderer (m.util.device-type/key-config-renderer-default device-type)]
            (set-device device)
            (when (and info-str-renderer config-renderer)
              (util/set-default-and-draft info-str-renderer config-renderer))))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/devices) :path route/devices}
       {:label (util.label/device-item device) :path (route/device-show id-device)}
       {:label util.label/logs}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links device {:id-item id-device}))
     (device-log.page/core
      #(model.device-log/fetch-list-and-total-for-device
        (assoc % :id-device id-device))
      {:map-default {:str-where "[]"}
       :on-receive on-receive})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
