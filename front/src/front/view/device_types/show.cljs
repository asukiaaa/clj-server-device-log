(ns front.view.device-types.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device-type :as model.device-type]
            [front.model.util.device-type :as model.util.device-type]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id (get params "device_type_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-type/fetch-by-id
        {:id id
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/device-types :path route/device-types}
                           {:label (util.label/device-type-item item)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:> router/Link {:to (route/device-type-edit id)} util.label/edit]
          " "
          [:f> util/btn-confirm-delete
           {:message-confirm (model.device-type/build-confirmation-message-for-deleting item)
            :action-delete #(model.device-type/delete {:id (:id item)
                                                       :on-receive (fn [] (navigate route/device-types))})}]
          " "
          [:> router/Link {:to (route/device-type-device-type-api-keys (:id item))} util.label/api-keys]
          " "
          [:> router/Link {:to (route/device-type-device-logs (:id item))} util.label/logs]

          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key model.util.device-type/keys-for-table]
              [:tr {:key key}
               [:td key]
               [:td (get item key)]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
