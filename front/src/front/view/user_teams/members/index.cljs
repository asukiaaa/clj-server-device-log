(ns front.view.user-teams.members.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.view.user-teams.util :as v.team.util]
            [front.view.user-teams.members.util :as v.team-member.util]
            [front.model.user-team-member :as model.user-team-member]
            [front.model.util.user-team :as util.user-team]))

(defn render-item [item on-delete]
  (let [id (:id item)
        id-user-team (:user_team_id item)]
    [:tr
     [:td (util.label/user-item (:member item))]
     [:td (:permission item)]
     [:td (:updated_at item)]
     [:td
      (util/render-list-inline
       (v.team-member.util/build-related-links item {:id-item id-user-team}))]]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-user-team (get params "user_team_id")
        [user-team set-user-team] (react/useState)
        labels-header [(util.label/member) (util.label/permission) (util.label/updated-at) (util.label/action)]
        fetch-list-and-total
        (fn [params]
          (model.user-team-member/fetch-list-and-total-for-user-team
           (merge params
                  {:id-user-team id-user-team})))
        on-receive
        (fn [result _errors]
          (set-user-team (util.user-team/key-table result)))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label (util.label/members)}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links user-team {:id-item id-user-team}))
     [util/area-content
      [:> router/Link {:to (route/user-team-member-create id-user-team)} (util.label/create)]]
     [:f> util.table/core fetch-list-and-total labels-header render-item
      {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
