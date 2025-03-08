(ns front.view.device-types.user-team-configs.select-team
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team :as model.user-team]
            [front.view.common.component.pagination :as pagination]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]))

(defn render-user-team [user-team id-device-type]
  [:tr
   [:td [:> router/Link {:to (route/user-team-show (:id user-team))} (:name user-team)]]
   [:td
    [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type (:id user-team))}
     util.label/edit]]])

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        [list-and-total set-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (:page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        [device-type set-device-type] (react/useState)]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team/fetch-list-and-total-for-device-type
        {:limit number-limit
         :page number-page
         :device_type_id id-device-type
         :on-receive
         (fn [data errors]
           (set-device-type (model.device-type/key-table data))
           (set-list-and-total data)
           (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/user-team-configs :path (route/device-type-user-team-configs id-device-type)}
       {:label util.label/select-team}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div util.label/assign-user-team-via-device-to-list-up]
        [:div util.label/total " " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th util.label/name]
           [:th util.label/user-team-config]]]
         [:tbody
          (for [item (:list list-and-total)]
            [:<> {:key (:id item)}
             [:f> render-user-team item id-device-type]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
