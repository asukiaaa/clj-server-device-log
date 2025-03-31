(ns front.view.devices.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.devices.util :as v.device.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device :as model.device]
            [front.model.user :as model.user]
            [front.model.util.device-type :as util.device-type]
            [front.model.util.user-team :as util.user-team]
            [front.model.util.user-team-device-config :as util.user-team-device-config]
            [front.model.util.device :as util.device]
            [front.view.users.util :as v.user.util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id (get params "device_id")
        navigate (router/useNavigate)
        user (util/get-user-loggedin)
        is-admin (model.user/admin? user)
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        fetch-bearer #(model.device/fetch-authorization-bearer-by-id
                       (merge % {:id id}))
        on-fetched-bearer
        (fn [bearer errors]
          (wrapper.fetching/finished info-wrapper-fetching errors))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device/fetch-by-id
        {:id id
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/devices) :path route/devices}
                           {:label (util.label/device-item item)}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links item {:id-item id}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            [:tr [:td util.label/id] [:td (:id item)]]
            [:tr [:td (util.label/name)] [:td (:name item)]]
            [:tr
             [:td (util.label/active-watch-scope)]
             [:td (v.device.util/render-active-watch-scope-terms item {:item-wrapper :div})]]
            (when is-admin
              [:tr
               [:td util.label/authorization-bearer]
               [:td [:f> util/button-to-fetch-authorization-bearer fetch-bearer {:on-fetched on-fetched-bearer}]]])
            [:tr
             [:td (util.label/device-type)]
             (let [device-type (util.device-type/key-table item)]
               [:td [:> router/Link {:to (route/device-type-show (:id device-type))} (util.label/device-type-item device-type)]])]
            [:tr
             [:td (util.label/user-team)]
             (let [user-team (util.user-team/key-table item)]
               [:td [:> router/Link {:to (route/user-team-show (:id user-team))} (util.label/user-team-item user-team)]])]
            [:tr [:td (util.label/user-team-config)] [:td (-> item util.user-team-device-config/key-table :config)]]
            (when is-admin
              [:tr
               [:td (util.label/action)]
               [:td [:f> util/btn-confirm-delete
                     {:message-confirm (model.device/build-confirmation-message-for-deleting item)
                      :action-delete #(model.device/delete {:id (:id item)
                                                            :on-receive (fn [] (navigate route/devices))})}]]])
            (for [key (->> [:created_at :updated_at]
                           (remove nil?))]
              [:tr {:key key}
               [:td (cond (= key :created_at) util.label/created-at
                          (= key :updated_at) util.label/updated-at)]
               [:td (let [val (key item)]
                      val)]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
