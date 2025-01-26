(ns front.view.device-types.device-type-api-keys.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-type-api-key :as model.device-type-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- render-key-str [device-type-api-key]
  (let [key-str (:key_str device-type-api-key)]
    [:div
     [:div key-str]
     [:button.btn.btn-sm.btn-secondary {:on-click #(util/copy-to-clipboard key-str)} util.label/copy]]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-device-type (get params "device_type_id")
        id-device-type-api-key (get params "device_type_api_key_id")
        [device-type set-device-type] (react/useState)
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-type-api-key/fetch-by-id-for-device-type
        {:id-device-type-api-key id-device-type-api-key
         :id-device-type id-device-type
         :on-receive (fn [item errors]
                       (set-item item)
                       (set-device-type (model.device-type/key-table item))
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys :path (route/device-type-device-type-api-keys id-device-type)}
       {:label (util.label/api-key-item item)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:> router/Link {:to (route/device-type-device-type-api-key-edit id-device-type id-device-type-api-key)} util.label/edit]
          " "
          [:f> util/btn-confirm-delete
           {:message-confirm (model.device-type-api-key/build-confirmation-message-for-deleting item)
            :action-delete #(model.device-type-api-key/delete
                             {:id (:id item)
                              :on-receive (fn [] (navigate (route/device-type-device-type-api-keys id-device-type)))})}]
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
                  :else (get item key))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
