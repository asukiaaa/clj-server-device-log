(ns front.view.user-teams.members.index
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
            [front.model.user-team-member :as model.user-team-member]
            [front.model.util.user-team :as util.user-team]))

(defn render-item [item on-delete]
  (let [id (:id item)
        id-user-team (:user_team_id item)]
    [:tr
     [:td (:id item)]
     [:td (util.label/user-item (:member item))]
     [:td (:permission item)]
     [:td (:updated_at item)]
     [:td
      [:> router/Link {:to (route/user-team-member-show id-user-team id)} (util.label/show)]
      " "
      [:> router/Link {:to (route/user-team-member-edit id-user-team id)} (util.label/edit)]
      " "
      [:f> util/btn-confirm-delete
       {:message-confirm (model.user-team-member/build-confirmation-message-for-deleting item)
        :action-delete #(model.user-team-member/delete {:id id :on-receive on-delete})}]]]))

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
          (model.user-team-member/fetch-list-and-total-for-user-team
           {:limit number-limit
            :page number-page
            :id-user-team id-user-team
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
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label util.label/members}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links user-team))
     [:> router/Link {:to (route/user-team-member-create id-user-team)} (util.label/create)]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "member"]
           [:th "permission"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [item received-list]
            [:<> {:key (:id item)}
             [:f> render-item item load-list]])]]
        [:f> pagination/core {:total-page number-total-page}]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
