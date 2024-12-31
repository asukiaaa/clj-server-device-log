(ns front.view.device-groups.device-group-api-keys.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.device-group-api-key :as model.device-group-api-key]))

(defn render-device-group-api-key [device-group-api-key on-delete]
  [:tr
   [:td (:id device-group-api-key)]
   [:td (:name device-group-api-key)]
   [:td (:permission device-group-api-key)]
   [:td (:updated_at device-group-api-key)]
   [:td
    [:> router/Link {:to (route/device-group-device-group-api-key-show (:device_group_id device-group-api-key) (:id device-group-api-key))} "show"]
    " "
    [:> router/Link {:to (route/device-group-device-group-api-key-edit (:device_group_id device-group-api-key) (:id device-group-api-key))} "edit"]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device-group-api-key/build-confirmation-message-for-deleting device-group-api-key)
      :action-delete #(model.device-group-api-key/delete {:id (:id device-group-api-key) :on-receive on-delete})}]]])

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device-group (get params "id_device_group")
        location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" route/device-group-device-group-api-keys page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-group-api-key/fetch-list-and-total-for-device-group
                     {:limit number-limit
                      :page number-page
                      :id-device-group id-device-group
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:> router/Link {:to (route/device-group-device-group-api-key-create id-device-group)} "new"]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "name"]
           [:th "permission"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-group-api-key item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
