(ns front.view.device-types.device-type-api-keys.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-type-api-key :as model.device-type-api-key]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn render-device-type-api-key [device-type-api-key on-delete]
  [:tr
   [:td (:id device-type-api-key)]
   [:td (:name device-type-api-key)]
   [:td (:permission device-type-api-key)]
   [:td
    [:a {:href "#"
         :on-click
         (fn [e]
           (.preventDefault e)
           (util/copy-to-clipboard (:key_str device-type-api-key)))} util.label/copy]]
   [:td (:updated_at device-type-api-key)]
   [:td
    [:> router/Link {:to (route/device-type-device-type-api-key-show (:device_type_id device-type-api-key) (:id device-type-api-key))} util.label/show]
    " "
    [:> router/Link {:to (route/device-type-device-type-api-key-edit (:device_type_id device-type-api-key) (:id device-type-api-key))} util.label/edit]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.device-type-api-key/build-confirmation-message-for-deleting device-type-api-key)
      :action-delete #(model.device-type-api-key/delete {:id (:id device-type-api-key) :on-receive on-delete})}]]])

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        location (router/useLocation)
        [device-type set-device-type] (react/useState)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" route/device-type-device-type-api-keys page number-limit))
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.device-type-api-key/fetch-list-and-total-for-device-type
           {:limit number-limit
            :page number-page
            :id-device-type id-device-type
            :on-receive (fn [result errors]
                          (set-list-and-total result)
                          (set-device-type (model.device-type/key-table result))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys}]]
     [:> router/Link {:to (route/device-type-device-type-api-key-create id-device-type)} util.label/create]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th util.label/id]
           [:th util.label/name]
           [:th util.label/permission]
           [:th util.label/api-key]
           [:th util.label/updated-at]
           [:th util.label/action]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-type-api-key item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
