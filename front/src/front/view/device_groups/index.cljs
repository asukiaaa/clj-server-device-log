(ns front.view.device-groups.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.device-group :as model.device-group]))

(defn render-device-group [device-group on-delete]
  [:tr
   [:td (:id device-group)]
   [:td (:user_id device-group)]
   [:td (:name device-group)]
   [:td (:created_at device-group)]
   [:td (:updated_at device-group)]
   [:td
    [:> router/Link {:to (route/device-group-show (:id device-group))} "show"]
    " "
    [:> router/Link {:to (route/device-group-edit (:id device-group))} "edit"]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device-group/build-confirmation-message-for-deleting device-group)
      :action-delete #(model.device-group/delete {:id (:id device-group) :on-receive on-delete})}]]])

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
        (fn [page] (format "%s?page=%d&limit=%d" route/device-groups page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-group/fetch-list-and-total
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
     [:> router/Link {:to route/device-group-create} "new"]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "user_id"]
           [:th "name"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-group item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
