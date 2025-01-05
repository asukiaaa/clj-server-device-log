(ns front.view.device-watch-groups.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.device-watch-group :as model.device-watch-group]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id (get params "id_device_watch_group")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-watch-group/fetch-by-id
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
         [:> router/Link {:to route/device-watch-groups} "index"]
         " "
         [:> router/Link {:to (route/device-watch-group-edit id)} "edit"]
         " "
         [:f> util/btn-confirm-delete
          {:message-confirm (model.device-watch-group/build-confirmation-message-for-deleting item)
           :action-delete #(model.device-watch-group/delete {:id (:id item)
                                                       :on-receive (fn [] (navigate route/device-watch-groups))})}]
         [:table.table.table-sm
          [:thead
           [:tr
            [:th "key"]
            [:th "value"]]]
          [:tbody
           (for [key [:id :owner_user_id :name :memo :created_at :updated_at]]
             [:tr {:key key}
              [:td key]
              [:td (get item key)]])]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
