(ns front.view.watch-scopes.watch-scope-terms.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.watch-scope-term :as model.watch-scope-term]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn render-watch-scope-term [watch-scope-term on-delete]
  [:tr
   [:td (:id watch-scope-term)]
   [:td (:display_name watch-scope-term)]
   [:td
    (:device_id watch-scope-term)
    " "
    (:device_name watch-scope-term)]
   [:td (:updated_at watch-scope-term)]
   (let [id-watch-scope (:watch_scope_id watch-scope-term)
         id-watch-scope-term (:id watch-scope-term)]
     [:td
      [:> router/Link {:to (route/watch-scope-watch-scope-term-show id-watch-scope id-watch-scope-term)} "show"]
      " "
      [:> router/Link {:to (route/watch-scope-watch-scope-term-edit id-watch-scope id-watch-scope-term)} "edit"]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.watch-scope-term/build-confirmation-message-for-deleting watch-scope-term)
        :action-delete #(model.watch-scope-term/delete {:id id-watch-scope-term :on-receive on-delete})}]])])

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-watch-scope (get params "watch_scope_id")
        location (router/useLocation)
        [watch-scope set-watch-scope] (react/useState)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" (route/watch-scope-watch-scope-terms id-watch-scope) page number-limit))
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.watch-scope-term/fetch-list-and-total-for-watch-scope
           {:limit number-limit
            :page number-page
            :id-watch-scope id-watch-scope
            :on-receive (fn [result errors]
                          (set-list-and-total result)
                          (set-watch-scope (model.watch-scope/key-table result))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/watch-scopes :path route/watch-scopes}
       {:label (util.label/watch-scope-item watch-scope) :path (route/watch-scope-show watch-scope)}
       {:label util.label/terms}]]
     [:> router/Link {:to (route/watch-scope-watch-scope-term-create id-watch-scope)} util.label/create]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "display name"]
           [:th "device id name"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-watch-scope-term item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
