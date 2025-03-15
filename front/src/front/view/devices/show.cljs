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
            [front.model.user :as model.user]))

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
     [:f> breadcrumb/core [{:label util.label/devices :path route/devices}
                           {:label (util.label/device-item item)}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links item))
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
            (for [key (->> [:id :name (when is-admin :authorization_bearer) :device_type :user_team :user_team_device_config :created_at :updated_at (when is-admin :action)]
                           (remove nil?))]
              [:tr {:key key}
               [:td key]
               [:td
                (cond
                  (or (= key :device_type) (= key :user_team))
                  (let [val (key item)]
                    (str (:id val) " " (:name val)))
                  (= key :user_team_device_config)
                  (-> item key :config)
                  (= key :authorization_bearer)
                  [:f> util/button-to-fetch-authorization-bearer fetch-bearer {:on-fetched on-fetched-bearer}]
                  (= key :action)
                  [:f> util/btn-confirm-delete
                   {:message-confirm (model.device/build-confirmation-message-for-deleting item)
                    :action-delete #(model.device/delete {:id (:id item)
                                                          :on-receive (fn [] (navigate route/devices))})}]
                  :else
                  (str (get item key)))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
