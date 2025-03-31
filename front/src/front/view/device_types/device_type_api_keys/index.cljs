(ns front.view.device-types.device-type-api-keys.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-type-api-key :as model.device-type-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.view.util :as util]))

(defn render-device-type-api-key [device-type-api-key on-delete]
  [:tr
   [:td (:name device-type-api-key)]
   [:td (:permission device-type-api-key)]
   [:td
    [:> router/Link {:to (route/device-type-device-type-api-key-show (:device_type_id device-type-api-key) (:id device-type-api-key))} (util.label/show)]
    " "
    [:> router/Link {:to (route/device-type-device-type-api-key-edit (:device_type_id device-type-api-key) (:id device-type-api-key))} (util.label/edit)]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device-type-api-key/build-confirmation-message-for-deleting device-type-api-key)
      :action-delete #(model.device-type-api-key/delete {:id (:id device-type-api-key) :on-receive on-delete})}]]])

(defn- page []
  (let [labels-header [(util.label/name) util.label/permission (util.label/action)]
        params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        [device-type set-device-type] (react/useState)
        fetch-list-and-total
        (fn [params]
          (model.device-type-api-key/fetch-list-and-total-for-device-type
           (assoc params :id-device-type id-device-type)))
        on-receive
        (fn [data _errors]
          (set-device-type (model.device-type/key-table data)))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys}]]
     (util/render-list-in-area-content-line
      (v.device-type.util/build-related-links device-type {:id-item id-device-type}))
     [:f> util.table/core fetch-list-and-total labels-header render-device-type-api-key
      {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
