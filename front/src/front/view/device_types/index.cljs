(ns front.view.device-types.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.device-types.util :as v.device-type.util]
            [front.model.device-type :as model.device-type]))

(defn render-device-type [device-type on-delete]
  (let [id-device-type (:id device-type)]
    [:tr
     [:td id-device-type]
     #_[:td (:user_id device-type)]
     [:td (:name device-type)]
     #_[:td (:created_at device-type)]
     [:td (:updated_at device-type)]
     [:td
      [:> router/Link {:to (route/device-type-show id-device-type)} util.label/show]
      " "
      [:> router/Link {:to (route/device-type-edit id-device-type)} util.label/edit]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.device-type/build-confirmation-message-for-deleting device-type)
        :action-delete #(model.device-type/delete {:id id-device-type :on-receive on-delete})}]
      " "
      (for [[label link] (v.device-type.util/build-related-links id-device-type)]
        [:<> {:key label}
         " "
         [:> router/Link {:to link} label]])]]))

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
        (fn [page] (format "%s?page=%d&limit=%d" route/device-types page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-type/fetch-list-and-total
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
     [:f> breadcrumb/core [{:label util.label/device-types}]]
     [:> router/Link {:to route/device-type-create} util.label/create]
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
             [:f> render-device-type item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
