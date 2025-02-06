(ns front.view.devices.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device :as model.device]
            [front.model.user :as model.user]))

(defn- page []
  (let [params (js->clj (router/useParams))
        user (util/get-user-loggedin)
        navigate (router/useNavigate)
        id (get params "device_id")
        [item set-item] (react/useState)
        [fetching set-fetching] (react/useState)
        [bearer set-bearer] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        fetch-bearer
        (fn []
          (set-fetching true)
          (model.device/fetch-bearer-by-id
           {:id id
            :on-receive
            (fn [item errors]
              (set-bearer (:authorization_bearer item))
              (set-fetching false)
              (wrapper.fetching/finished info-wrapper-fetching errors))}))]
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
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          (when (model.user/admin? user)
            [:<>
             [:> router/Link {:to (route/device-edit id)} util.label/edit]
             " "
             [:f> util/btn-confirm-delete
              {:message-confirm (model.device/build-confirmation-message-for-deleting item)
               :action-delete #(model.device/delete {:id (:id item)
                                                     :on-receive (fn [] (navigate route/devices))})}]
             " "])
          [:> router/Link {:to (route/device-device-files (:id item))} util.label/files]
          " "
          [:> router/Link {:to (route/device-device-logs (:id item))} util.label/logs]
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key [:id :name :authorization_bearer :device_type :user_team :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td
                (cond
                  (or (= key :device_type) (= key :user_team))
                  (let [val (key item)]
                    (str (:id val) " " (:name val)))
                  (= key :authorization_bearer)
                  [:<>
                   (if bearer
                     [:f> util/link-to-copy-to-clipboard bearer]
                     [:button.btn.btn-primary.btn-sm
                      {:on-click fetch-bearer :disabled fetching}
                      util.label/get-bearer])]
                  :else
                  (str (get item key)))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
