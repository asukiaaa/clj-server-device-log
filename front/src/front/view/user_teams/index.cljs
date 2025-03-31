(ns front.view.user-teams.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.user-teams.util :as v.team.util]
            [front.view.util.table :as util.table]
            [front.model.user-team :as model.user-team]))

(defn render-user-team [user-team on-delete]
  [:tr
   [:td (:name user-team)]
   [:td
    (util/render-list-inline
     (v.team.util/build-related-links user-team))]])

(defn- page []
  (let [labels-header [(util.label/name) (util.label/action)]
        is-admin (util/detect-is-admin-loggedin)]
    [:<>
     [:f> breadcrumb/core [{:label (util.label/user-teams)}]]
     (when is-admin
       [util/area-content
        [:> router/Link {:to route/user-team-create} (util.label/create)]])
     [:f> util.table/core model.user-team/fetch-list-and-total labels-header render-user-team]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
