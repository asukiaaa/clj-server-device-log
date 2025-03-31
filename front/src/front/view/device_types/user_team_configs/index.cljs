(ns front.view.device-types.user-team-configs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team-device-type-config :as model.user-team-device-type-config]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.view.util :as util]))

(defn render-user-team-device-type-config [item on-delete]
  (let [id-device-type (:device_type_id item)
        id-user-team (:user_team_id item)]
    [:tr
     [:td [:> router/Link {:to (route/user-team-show id-user-team)} (util.label/user-team-item (:user_team item))]]
     [:td
      [:> router/Link {:to (route/device-type-user-team-config-show id-device-type id-user-team)} (util.label/show)]
      " "
      [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type id-user-team)} (util.label/edit)]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.user-team-device-type-config/build-confirmation-message-for-deleting item)
        :action-delete #(model.user-team-device-type-config/delete
                         {:device_type_id id-device-type :user_team_id id-user-team :on-receive on-delete})}]]]))

(defn- page []
  (let [labels-header [(util.label/user-team) (util.label/action)]
        params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        [device-type set-device-type] (react/useState)
        fetch-list-and-total
        (fn [params]
          (model.user-team-device-type-config/fetch-list-and-total-for-device-type
           (assoc params :device_type_id id-device-type)))
        on-receive
        (fn [data _errors]
          (set-device-type (model.device-type/key-table data)))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label (util.label/user-team-configs)}]]
     (util/render-list-in-area-content-line
      (v.device-type.util/build-related-links device-type {:id-item id-device-type}))
     [util/area-content
      [:> router/Link {:to (route/device-type-user-team-config-select-team id-device-type)} util.label/select-team]]
     [:f> util.table/core fetch-list-and-total labels-header render-user-team-device-type-config
      {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
