(ns front.view.devices.raw-device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.raw-device-log :as model.raw-device-log]))

(defn render-raw-device-log [raw-device-log on-delete]
  [:tr
   [:td (:id raw-device-log)]
   [:td (:data raw-device-log)]
   [:td (:created_at raw-device-log)]
   [:td (:updated_at raw-device-log)]
   [:td
    #_[:> router/Link {:to (route/raw-device-log-show (:id raw-device-log))} "show"]
    " "
    #_[:> router/Link {:to (route/raw-device-log-edit (:id raw-device-log))} "edit"]
    " "
    #_[:f> util/btn-confirm-delete
       {:message-confirm (model.raw-device-log/build-confirmation-message-for-deleting raw-device-log)
        :action-delete #(model.raw-device-log/delete {:id (:id raw-device-log) :on-receive on-delete})}]]])

#_(defn- page []
    [:div "hi"])

(defn-  page []
  (let [params (js->clj (router/useParams))
        id (get params "id_device")
        location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        build-url-by-page
        (fn [page] (format "%s?page=%d&limit=%d" (route/device-raw-device-logs id) page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.raw-device-log/fetch-list-and-total-for-device
                     {:id-device id
                      :limit number-limit
                      :page number-page
                      :str-order "{}"
                      :str-where "{}"
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
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "data"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-raw-device-log item on-delete]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
