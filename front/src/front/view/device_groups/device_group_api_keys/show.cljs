(ns front.view.device-groups.device-group-api-keys.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.device-group-api-key :as model.device-group-api-key]))

(defn- render-key-str [device-group-api-key]
  [:<>
   [:div
    [:div (:key_str device-group-api-key)]
    (let [key-post (model.device-group-api-key/build-key-post device-group-api-key)]
      [:<>
       [:pre.mb-0 key-post]
       [:button.btn.btn-sm.btn-secondary {:on-click #(util/copy-to-clipboard key-post)} "copy"]])]])

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-device-group (get params "id_device_group")
        id-device-group-api-key (get params "id_device_group_api_key")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-group-api-key/fetch-by-id-for-device-group
        {:id-device-group-api-key id-device-group-api-key
         :id-device-group id-device-group
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
         [:> router/Link {:to (route/device-group-device-group-api-keys id-device-group)} "index"]
         " "
         [:> router/Link {:to (route/device-group-device-group-api-key-edit id-device-group id-device-group-api-key)} "edit"]
         " "
         [:f> util/btn-confirm-delete
          {:message-confirm (model.device-group-api-key/build-confirmation-message-for-deleting item)
           :action-delete #(model.device-group-api-key/delete
                            {:id (:id item)
                             :on-receive (fn [] (navigate (route/device-group-device-group-api-keys id-device-group)))})}]
         [:table.table.table-sm
          [:thead
           [:tr
            [:th "key"]
            [:th "value"]]]
          [:tbody
           (for [key [:id :name :key_str :permission :created_at :updated_at]]
             [:tr {:key key}
              [:td key]
              [:td
               (cond
                 (= key :key_str) (render-key-str item)
                 :else (get item key))]])]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
