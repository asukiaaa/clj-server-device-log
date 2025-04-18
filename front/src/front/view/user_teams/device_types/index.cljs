(ns front.view.user-teams.device-types.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.view.user-teams.util :as v.team.util]
            [front.model.device-type :as model.device-type]
            [front.model.util.user-team :as util.user-team]))

(defn render-item [item id-user-team]
  (let [id-device-type (:id item)]
    [:tr
     [:td [:> router/Link {:to (route/device-type-show id-device-type)} (:name item)]]
     [:td
      [:> router/Link {:to (route/device-type-user-team-config-show id-device-type id-user-team)} (util.label/show)]
      " "
      [:> router/Link {:to (route/device-type-user-team-config-edit id-device-type id-user-team)} (util.label/edit)]]]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-user-team (get params "user_team_id")
        [user-team set-user-team] (react/useState)
        labels-header [(util.label/name) (util.label/device-type-config-on-user-team)]
        on-receive
        (fn [result _errors]
          (set-user-team (util.user-team/key-table result)))
        fetch-list-and-total
        (fn [params]
          (model.device-type/fetch-list-and-total-for-user-team
           (merge params {:user_team_id id-user-team})))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label (util.label/device-types)}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links user-team {:id-item id-user-team}))
     [:f> util.table/core fetch-list-and-total labels-header #(render-item % id-user-team)
      {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
