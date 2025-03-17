(ns front.view.user-teams.device-types.index
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
            [front.model.device-type :as model.device-type]
            [front.model.util.user-team :as util.user-team]))

(defn render-item [item id-user-team]
  (let [id-device-type (:id item)]
    [:tr
     [:td id-device-type]
     [:td [:> router/Link {:to (route/device-type-show id-device-type)} (:name item)]]
     [:td
      [:> router/Link {:to (route/device-type-user-team-config-show id-device-type id-user-team)} util.label/show]
      " "
      [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type id-user-team)} util.label/edit]]]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-user-team (get params "user_team_id")
        location (router/useLocation)
        [list-and-total set-list-and-total] (react/useState)
        [user-team set-user-team] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        received-list (:list list-and-total)
        total (:total list-and-total)
        query-params (util/read-query-params)
        number-page (or (pagination/key-page query-params) 0)
        number-limit (or (:limit query-params) 50)
        number-total-page (pagination/calc-total-page number-limit total)
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.device-type/fetch-list-and-total-for-user-team
           {:limit number-limit
            :page number-page
            :user_team_id id-user-team
            :on-receive (fn [result errors]
                          (set-list-and-total result)
                          (set-user-team (util.user-team/key-table result))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [location])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/user-teams :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label util.label/device-types}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links user-team))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div util.label/total " " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th util.label/id]
           [:th util.label/name]
           [:th util.label/device-type-config]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-item item id-user-team]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
