(ns front.view.users.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]))

(defn render-user [user on-delete]
  [:tr
   [:td (:id user)]
   [:td (:email user)]
   [:td (:name user)]
   [:td (:created_at user)]
   [:td (:updated_at user)]
   [:td
    [:> router/Link {:to (route/user-show (:id user))} util.label/show]
    " "
    [:> router/Link {:to (route/user-edit (:id user))} util.label/edit]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.user/build-confirmation-message-for-deleting user)
      :action-delete #(model.user/delete {:id (:id user) :on-receive on-delete})}]]])

(defn-  page []
  (let [location (router/useLocation)
        [user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        users (:list user-list-and-total)
        total (:total user-list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" route/users page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.user/fetch-list-and-total
                     {:limit number-limit
                      :page number-page
                      :on-receive (fn [result errors]
                                    (set-user-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core [{:label util.label/users}]]
     [:> router/Link {:to route/user-create} util.label/create]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "email"]
           [:th "name"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [user users]
            [:<> {:key (:id user)}
             [:f> render-user user load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
