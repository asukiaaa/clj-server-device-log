(ns front.view.device-watch-groups.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.device-watch-group :as model.device-watch-group]))

(defn render-device-watch-group [device-watch-group on-delete]
  [:tr
   [:td (:id device-watch-group)]
   #_[:td (:user_id device-watch-group)]
   [:td (:name device-watch-group)]
   [:td (:created_at device-watch-group)]
   [:td (:updated_at device-watch-group)]
   [:td
    [:> router/Link {:to (route/device-watch-group-raw-device-logs (:id device-watch-group))} "logs"]
    " "
    [:> router/Link {:to (route/device-watch-group-device-watch-group-devices (:id device-watch-group))} "devices"]
    " "
    [:> router/Link {:to (route/device-watch-group-show (:id device-watch-group))} "show"]
    " "
    [:> router/Link {:to (route/device-watch-group-edit (:id device-watch-group))} "edit"]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device-watch-group/build-confirmation-message-for-deleting device-watch-group)
      :action-delete #(model.device-watch-group/delete {:id (:id device-watch-group) :on-receive on-delete})}]]])

(defn-  page []
  (let [location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (pagination/key-page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-watch-group/fetch-list-and-total
                     {:limit number-limit
                      :page number-page
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:> router/Link {:to route/device-watch-group-create} "new"]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           #_[:th "user_id"]
           [:th "name"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-watch-group item load-list]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
