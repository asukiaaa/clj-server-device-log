(ns front.view.device-types.device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-log :as model.device-log]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn render-device-log [device-log on-delete]
  [:tr
   [:td (:id device-log)]
   [:td (:device_id device-log) " " (:device_name device-log)]
   [:td (:data device-log)]
   [:td (:created_at device-log)]
   [:td
    #_[:> router/Link {:to (route/device-log-show (:id device-log))} "show"]
    " "
    #_[:> router/Link {:to (route/device-log-edit (:id device-log))} "edit"]
    " "
    #_[:f> util/btn-confirm-delete
       {:message-confirm (model.device-log/build-confirmation-message-for-deleting device-log)
        :action-delete #(model.device-log/delete {:id (:id device-log) :on-receive on-delete})}]]])

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
        (fn [page] (format "%s?page=%d&limit=%d" (route/device-type-device-logs id-device-type) page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-log/fetch-list-and-total-for-device-type
                     {:id-device-type id-device-type
                      :limit number-limit
                      :page number-page
                      :str-order "{}"
                      :str-where "{}"
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (set-device-type (model.device-type/key-table result))
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
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "device"]
           [:th "data"]
           [:th "created_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-log item on-delete]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
