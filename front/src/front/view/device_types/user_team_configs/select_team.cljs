(ns front.view.device-types.user-team-configs.select-team
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]))

(defn build-renderer-user-team [id-device-type]
  (fn [user-team]
    [:tr
     [:td [:> router/Link {:to (route/user-team-show (:id user-team))} (:name user-team)]]
     [:td
      [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type (:id user-team))}
       util.label/edit]]]))

(defn- page []
  (let [labels-header [util.label/user-team util.label/action]
        params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        [device-type set-device-type] (react/useState)
        fetch-list-and-total
        (fn [params]
          (model.user-team/fetch-list-and-total-for-device-type
           (assoc params :device_type_id id-device-type)))
        on-receive
        (fn [data errors]
          (set-device-type (model.device-type/key-table data)))]
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/user-team-configs :path (route/device-type-user-team-configs id-device-type)}
       {:label util.label/select-team}]]
     [util/area-content
      util.label/assign-device-to-user-team-to-list-up]
     [:f> util.table/core fetch-list-and-total labels-header (build-renderer-user-team id-device-type)
      {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
