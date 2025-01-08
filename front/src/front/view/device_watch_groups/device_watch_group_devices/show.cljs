(ns front.view.device-watch-groups.device-watch-group-devices.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.device-watch-group-device :as model.device-watch-group-device]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-device-watch-group (get params "id_device_watch_group")
        id-device-watch-group-device (get params "id_device_watch_group_device")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-watch-group-device/fetch-by-id-for-device-watch-group
        {:id id-device-watch-group-device
         :id-device-watch-group id-device-watch-group
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:> router/Link {:to (route/device-watch-group-device-watch-group-devices id-device-watch-group)} "index"]
         " "
         [:> router/Link {:to (route/device-watch-group-device-watch-group-device-edit id-device-watch-group id-device-watch-group-device)} "edit"]
         " "
         [:f> util/btn-confirm-delete
          {:message-confirm (model.device-watch-group-device/build-confirmation-message-for-deleting item)
           :action-delete #(model.device-watch-group-device/delete
                            {:id (:id item)
                             :on-receive (fn [] (navigate (route/device-watch-group-device-watch-group-devices id-device-watch-group)))})}]
         [:table.table.table-sm
          [:thead
           [:tr
            [:th "key"]
            [:th "value"]]]
          [:tbody
           (for [key [:id :display_name :device :device_watch_group_id :created_at :updated_at]]
             [:tr {:key key}
              [:td (cond
                     (= :device key) "device id name"
                     :else key)]
              [:td (cond
                     (= :device key) (str (:device_id item) " " (:device_name item))
                     :else (get item key))]])]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
