(ns front.view.devices.raw-device-logs.index
  (:require ["react-router-dom" :as router]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.raw-device-log :as model.raw-device-log]
            [front.view.util.raw-device-log.page :as raw-device-log.page]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device (get params "id_device")]
    (raw-device-log.page/core
     (fn [params] (model.raw-device-log/fetch-list-and-total-for-device
                   (assoc params :id-device id-device)))
     {:map-default {:str-where "[]"}})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
