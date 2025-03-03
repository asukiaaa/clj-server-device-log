(ns front.view.devices.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.devices.util :as v.device.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.user :as model.user]
            [front.model.device :as model.device]))

(defn render-device [device on-delete user]
  [:tr
   [:td (:id device)]
   [:td (:name device)]
   [:td [:> router/Link {:to (route/device-type-show (-> device :device_type :id))}
         (-> device :device_type :name)]]
   [:td [:> router/Link {:to (route/user-team-show (-> device :user_team :id))}
         (-> device :user_team :name)]]
   #_[:td (:created_at device)]
   #_[:td (:updated_at device)]
   [:td

    [:> router/Link {:to (route/device-show (:id device))} util.label/show]
    " "
    (when (model.user/admin? user)
      [:<>
       [:> router/Link {:to (route/device-edit (:id device))} util.label/edit]
       " "
       [:f> util/btn-confirm-delete
        {:message-confirm (model.device/build-confirmation-message-for-deleting device)
         :action-delete #(model.device/delete {:id (:id device) :on-receive on-delete})}]])
    (for [[label link] (v.device.util/build-related-links (:id device))]
      [:<> {:key label}
       " "
       [:> router/Link {:to link} label]])]])

(defn-  page []
  (let [location (router/useLocation)
        user (util/get-user-loggedin)
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
     [:f> breadcrumb/core [{:label util.label/devices}]]
     (when (model.user/admin? user)
       [:> router/Link {:to route/device-create} util.label/create])
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
           [:th "device_type"]
           [:th "user_team"]
           #_[:th "created_at"]
           #_[:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device item on-delete user]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
