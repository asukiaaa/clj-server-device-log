(ns front.view.device-types.user-team-configs.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team :as model.user-team]
            [front.model.user-team-device-type-config :as model.user-team-device-type-config]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-device-type (get params "device_type_id")
        id-user-team (get params "user_team_id")
        [device-type set-device-type] (react/useState)
        [user-team set-user-team] (react/useState)
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team-device-type-config/fetch-by-user-team-and-device-type
        {:user_team_id id-user-team
         :device_type_id id-device-type
         :on-receive (fn [item errors]
                       (set-item item)
                       (set-device-type (model.device-type/key-table item))
                       (set-user-team (model.user-team/key-table item))
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/user-team-configs :path (route/device-type-user-team-configs id-device-type)}
       {:label (util.label/user-team-item (:user_team item))}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div util.label/no-data]
         [:div
          [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type id-user-team)} util.label/edit]
          " "
          [:f> util/btn-confirm-delete
           {:message-confirm (model.user-team-device-type-config/build-confirmation-message-for-deleting item)
            :action-delete #(model.user-team-device-type-config/delete
                             {:user_team_id id-user-team
                              :device_type_id id-device-type
                              :on-receive (fn [] (navigate (route/device-type-user-team-configs id-device-type)))})}]
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key [:device_type :user_team :config :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td
                (cond
                  (= key :user_team)
                  [:> router/Link {:to (route/user-team-show id-user-team)} (util.label/user-team-item user-team)]
                  (= key :device_type)
                  [:> router/Link {:to (route/device-type-show id-device-type)} (util.label/device-type-item device-type)]
                  :else (get item key))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
