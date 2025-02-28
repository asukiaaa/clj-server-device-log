(ns front.view.user-teams.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.user-teams.util :as v.team.util]
            [front.model.user-team :as model.user-team]))

(defn render-user-team [user-team on-delete]
  [:tr
   [:td (:id user-team)]
   #_[:td (:user_id user-team)]
   [:td (:name user-team)]
   #_[:td (:created_at user-team)]
   [:td (:updated_at user-team)]
   [:td
    ;; [:> router/Link {:to (route/user-team-device-logs (:id user-team))} "logs"]
    ;; " "
    ;; [:> router/Link {:to (route/user-team-user-team-devices (:id user-team))} "devices"]
    ;; " "
    [:> router/Link {:to (route/user-team-show (:id user-team))} util.label/show]
    " "
    [:> router/Link {:to (route/user-team-edit (:id user-team))} util.label/edit]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.user-team/build-confirmation-message-for-deleting user-team)
      :action-delete #(model.user-team/delete {:id (:id user-team) :on-receive on-delete})}]
    (for [[label link] (v.team.util/build-related-links (:id user-team))]
      [:<> {:key label}
       " "
       [:> router/Link {:to link} label]])]])

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
                    (model.user-team/fetch-list-and-total
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
     [:f> breadcrumb/core [{:label util.label/user-teams}]]
     [:> router/Link {:to route/user-team-create} util.label/create]
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
           #_[:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-user-team item load-list]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
