(ns front.view.devices.device-files.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-file :as model.device-file]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.devices.util :as v.device.util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.device-file.page :as file.page]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        [device set-device] (react/useState)
        on-receive
        (fn [result _errors]
          (set-device (:device result)))
        fetch-list-and-total
        (fn [params]
          (model.device-file/fetch-list-and-total-for-device
           (assoc params :id-device id-device)))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/devices) :path route/devices}
       {:label (util.label/device-item device) :path (route/device-show id-device)}
       {:label util.label/files}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links device))
     [:f> file.page/core fetch-list-and-total {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
