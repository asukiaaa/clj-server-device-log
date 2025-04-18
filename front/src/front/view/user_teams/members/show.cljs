(ns front.view.user-teams.members.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.user-teams.members.util :as v.team-member.util]
            [front.view.util.label :as util.label]
            [front.model.user-team-member :as model.user-team-member]
            [front.model.user-team :as model.user-team]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-item (get params "user_team_member_id")
        id-user-team (get params "user_team_id")
        [item set-item] (react/useState)
        [user-team set-user-team] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team-member/fetch-by-id-for-user-team
        {:id id-item
         :user_team_id id-user-team
         :on-receive (fn [item errors]
                       (set-item item)
                       (set-user-team (model.user-team/key-table item))
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label (util.label/members) :path (route/user-team-members id-user-team)}
       {:label (util.label/user-team-member-item item)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [util/area-content (util.label/no-data)]
         [:<>
          (util/render-list-in-area-content-line
           (v.team-member.util/build-related-links item {:id-item id-user-team}))
          [:table.table.table-sm
           [:thead
            [:tr
             [:th (util.label/element)]
             [:th (util.label/value)]]]
           [:tbody
            [:tr
             [:td (util.label/id)]
             [:td (:id item)]]
            [:tr
             [:td (util.label/user)]
             [:td
              (let [member (:member item)]
                [:> router/Link {:to (route/user-show (:id member))} (util.label/user-item member)])]]
            [:tr
             [:td (util.label/permission)]
             [:td [:pre.m-0 (:permission item)]]]
            [:tr
             [:td (util.label/created-at)]
             [:td (:created_at item)]]
            [:tr
             [:td (util.label/updated-at)]
             [:td (:updated_at item)]]
            [:tr
             [:td (util.label/action)]
             [:td
              [:f> util/btn-confirm-delete
               {:message-confirm (model.user-team-member/build-confirmation-message-for-deleting item)
                :action-delete #(model.user-team-member/delete
                                 {:id (:id item)
                                  :on-receive (fn [] (navigate (route/user-team-members id-user-team)))})}]]]]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
