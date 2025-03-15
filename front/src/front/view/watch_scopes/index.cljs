(ns front.view.watch-scopes.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util.table :as util.table]
            [front.view.util :as util]
            [front.view.watch-scopes.util :as v.watch-scope.util]))

(defn render-watch-scope [watch-scope]
  [:tr
   [:td (:name watch-scope)]
   [:td (let [team (:user_team watch-scope)]
          [:> router/Link {:to (route/user-team-show (:id team))} (util.label/user-team-item team)])]
   [:td (util.watch-scope/render-terms (:terms watch-scope))]
   [:td
    (util/render-list-inline
     (v.watch-scope.util/build-related-links watch-scope))]])

(defn- page []
  (let [labels-header [util.label/name util.label/user-team util.label/term util.label/action]]
    [:<>
     [:f> breadcrumb/core [{:label util.label/watch-scopes}]]
     [util/area-content
      [:> router/Link {:to route/watch-scope-create} util.label/create]]
     [:f> util.table/core model.watch-scope/fetch-list-and-total labels-header render-watch-scope]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
