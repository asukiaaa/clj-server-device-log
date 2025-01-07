(ns front.view.device-watch-groups.device-watch-group-devices.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.model.device-watch-group-device :as model.device-watch-group-device]))

(defn render-device-watch-group-device [device-watch-group-device on-delete]
  [:tr
   [:td (:id device-watch-group-device)]
   [:td (:name_device device-watch-group-device)]
   [:td (:device_id device-watch-group-device)]
   [:td (:updated_at device-watch-group-device)]
   (let [id-device-watch-group (:device_watch_group_id device-watch-group-device)
         id-device-watch-group-device (:id device-watch-group-device)]
     [:td
      [:> router/Link {:to (route/device-watch-group-device-watch-group-device-show id-device-watch-group id-device-watch-group-device)} "show"]
      " "
      [:> router/Link {:to (route/device-watch-group-device-watch-group-device-edit id-device-watch-group id-device-watch-group-device)} "edit"]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.device-watch-group-device/build-confirmation-message-for-deleting device-watch-group-device)
        :action-delete #(model.device-watch-group-device/delete {:id id-device-watch-group-device :on-receive on-delete})}]])])

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-device-watch-group (get params "id_device_watch_group")
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
        (fn [page] (format "%s?page=%d&limit=%d" (route/device-watch-group-device-watch-group-devices id-device-watch-group) page number-limit))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-watch-group-device/fetch-list-and-total-for-device-watch-group
                     {:limit number-limit
                      :page number-page
                      :id-device-watch-group id-device-watch-group
                      :on-receive (fn [result errors]
                                    (set-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:> router/Link {:to (route/device-watch-group-device-watch-group-device-create id-device-watch-group)} "new"]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "name device"]
           [:th "device id"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-device-watch-group-device item load-list]])]]
        [:f> pagination/core {:build-url build-url-by-page
                              :total-page number-total-page
                              :current-page number-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
