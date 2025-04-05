(ns front.view.device-types.device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-log :as model.device-log]
            [front.model.util.device-type :as m.util.device-type]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util :as util]
            [front.view.util.device-log.page :as device-log.page]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        [device-type set-device-type] (react/useState)
        on-receive
        (fn [data errors {:keys [info-str-renderer]}]
          (let [device-type (m.util.device-type/key-table data)
                config-renderer-default (m.util.device-type/key-config-renderer-default device-type)]
            (when (and info-str-renderer config-renderer-default)
              (util/set-default-and-draft info-str-renderer config-renderer-default))
            (set-device-type device-type)))
        fetch-list-and-total
        (fn [params]
          (model.device-log/fetch-list-and-total-for-device-type
           (merge params {:id-device-type id-device-type})))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/logs}]]
     (util/render-list-in-area-content-line
      (v.device-type.util/build-related-links device-type {:id-item id-device-type}))
     (device-log.page/core fetch-list-and-total {:on-receive on-receive})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
