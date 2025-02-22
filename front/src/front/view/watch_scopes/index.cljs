(ns front.view.watch-scopes.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util :as util]))

(defn render-watch-scope [watch-scope on-delete]
  [:tr
   [:td (:name watch-scope)]
   [:td (let [team (:user_team watch-scope)]
          [:> router/Link {:to (route/user-team-show (:id team))} (util.label/user-team-item team)])]
   [:td (util.watch-scope/render-terms (:terms watch-scope))]
   [:td
    [:> router/Link {:to (route/watch-scope-show (:id watch-scope))} util.label/show]
    " "
    [:> router/Link {:to (route/watch-scope-edit (:id watch-scope))} util.label/edit]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.watch-scope/build-confirmation-message-for-deleting watch-scope)
      :action-delete #(model.watch-scope/delete {:id (:id watch-scope) :on-receive on-delete})}]
    " "
    [:> router/Link {:to (route/watch-scope-device-logs (:id watch-scope))} util.label/logs]]])

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
                    (model.watch-scope/fetch-list-and-total
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
     [:f> breadcrumb/core [{:label util.label/watch-scopes}]]
     [:> router/Link {:to route/watch-scope-create} util.label/create]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "name"]
           [:th util.label/user-team]
           [:th "terms"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-watch-scope item load-list]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
