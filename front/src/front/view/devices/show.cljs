(ns front.view.devices.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.device :as model.device]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id (get params "id_device")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
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
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:> router/Link {:to route/devices} "index"]
         " "
         [:> router/Link {:to (route/device-edit id)} "edit"]
         " "
         [:f> util/btn-confirm-delete
          {:message-confirm (model.device/build-confirmation-message-for-deleting item)
           :action-delete #(model.device/delete {:id (:id item)
                                                 :on-receive (fn [] (navigate route/devices))})}]
         [:table.table.table-sm
          [:thead
           [:tr
            [:th "key"]
            [:th "value"]]]
          [:tbody
           (for [key [:id :name :device_group :created_at :updated_at]]
             [:tr {:key key}
              [:td key]
              [:td
               #_(get item key)
               (case key
                 :device_group (let [device-group (:device_group item)]
                                 (str device-group)
                                 #_(str (:device_group_id item) " " (:name device-group)))
                 (get item key))]])]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
