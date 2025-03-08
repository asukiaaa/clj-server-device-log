(ns front.view.device-types.user-team-configs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team-device-type-config :as model.user-team-device-type-config]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn render-user-team-device-type-config [item on-delete]
  (let [id-device-type (:device_type_id item)
        id-user-team (:user_team_id item)]
    [:tr
     [:td [:> router/Link {:to (route/user-team-show id-user-team)} (util.label/user-team-item (:user_team item))]]
     [:td
      [:> router/Link {:to (route/device-type-user-team-config-show id-device-type id-user-team)} util.label/show]
      " "
      [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type id-user-team)} util.label/edit]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.user-team-device-type-config/build-confirmation-message-for-deleting item)
        :action-delete #(model.user-team-device-type-config/delete
                         {:device_type_id id-device-type :user_team_id id-user-team :on-receive on-delete})}]]]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        location (router/useLocation)
        [device-type set-device-type] (react/useState)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.user-team-device-type-config/fetch-list-and-total-for-device-type
           {:limit number-limit
            :page number-page
            :device_type_id id-device-type
            :on-receive (fn [result errors]
                          (set-list-and-total result)
                          (set-device-type (model.device-type/key-table result))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/user-team-configs}]]
     [:> router/Link {:to (route/device-type-user-team-config-select-team id-device-type)} util.label/select-team]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div util.label/total " " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th util.label/user-team]
           [:th util.label/action]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-user-team-device-type-config item load-list]])]]
        [:f> pagination/core {:total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
