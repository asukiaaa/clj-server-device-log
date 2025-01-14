(ns front.view.devices.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.device :as model.device]))

(defn render-device [device on-delete]
  [:tr
   [:td (:id device)]
   [:td (:name device)]
   [:td (-> device :device_group :name)]
   [:td (:created_at device)]
   [:td (:updated_at device)]
   [:td
    [:> router/Link {:to (route/device-device-files (:id device))} "files"]
    " "
    [:> router/Link {:to (route/device-raw-device-logs (:id device))} "logs"]
    " "
    [:> router/Link {:to (route/device-show (:id device))} "show"]
    " "
    [:> router/Link {:to (route/device-edit (:id device))} "edit"]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device/build-confirmation-message-for-deleting device)
      :action-delete #(model.device/delete {:id (:id device) :on-receive on-delete})}]]])

(defn-  page []
  (let [location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" route/devices page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device/fetch-list-and-total
                     {:limit number-limit
                      :page number-page
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))
        on-delete (fn [_data errors]
                    (wrapper.fetching/set-errors info-wrapper-fetching errors)
                    (when (empty? errors)
                      (load-list)))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:> router/Link {:to route/device-create} "new"]
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
           [:th "device_group"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device item on-delete]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
