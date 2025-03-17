(ns front.view.devices.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.devices.util :as v.device.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.model.user :as model.user]
            [front.model.device :as model.device]))

(defn render-device [device on-delete]
  [:tr
   [:td (:name device)]
   [:td [:> router/Link {:to (route/device-type-show (-> device :device_type :id))}
         (-> device :device_type :name)]]
   [:td [:> router/Link {:to (route/user-team-show (-> device :user_team :id))}
         (-> device :user_team :name)]]
   [:td (v.device.util/render-active-watch-scope-terms device)]
   [:td
    (util/render-list-inline
     (v.device.util/build-related-links device))]])

(defn- page []
  (let [labels-header [util.label/name util.label/device-type util.label/user-team util.label/active-watch-scope util.label/action]
        user-loggedin (util/get-user-loggedin)]
    [:<>
     [:f> breadcrumb/core [{:label util.label/devices}]]
     (when (model.user/admin? user-loggedin)
       [util/area-content
        [:> router/Link {:to route/device-create} util.label/create]])
     [:f> util.table/core model.device/fetch-list-and-total labels-header render-device]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
