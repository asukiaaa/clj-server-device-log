(ns front.view.devices.device-logs.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.devices.util :as v.device.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device-log :as model.device-log]
            [front.model.user :as model.user]
            [front.model.util.device :as util.device]
            [front.model.util.device-type :as util.device-type]
            [front.model.util.user-team :as util.user-team]
            [front.model.util.user-team-device-config :as util.user-team-device-config]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        id-device-log (get params "device_log_id")
        navigate (router/useNavigate)
        user (util/get-user-loggedin)
        is-admin (model.user/admin? user)
        [item set-item] (react/useState)
        device (util.device/key-table item)
        device-type (util.device-type/key-table device)
        id-device-type (:id device-type)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-log/fetch-by-id-for-device
        {:id id-device-log
         :id-device id-device
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/devices) :path route/devices}
                           {:label (util.label/device-item device) :path (route/device-show id-device)}
                           {:label util.label/logs :path (route/device-device-logs id-device)}
                           {:label (str id-device-log)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:table.table.table-sm.mb-1
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            [:tr [:td util.label/id] [:td (:id item)]]
            [:tr
             [:td (util.label/device)]
             [:td
              [:> router/Link {:to (route/device-show id-device)}
               (util.label/device-item device)]]]
            [:tr
             [:td (util.label/device-type)]
             [:td
              [:> router/Link {:to (route/device-type-show id-device-type)}
               (util.label/device-type-item device-type)]]]
            [:tr
             [:td util.label/created-at]
             [:td (:created_at item)]]]]
          [:div.m-1
           [:div (util.label/data)]
           [:pre
            (.stringify js/JSON (.parse js/JSON (:data item)) nil 2)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
