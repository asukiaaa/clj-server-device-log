(ns front.view.watch-scopes.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util.table :as util.table]
            [front.view.util :as util]))

(defn render-watch-scope [watch-scope load-list]
  [:tr
   [:td (:name watch-scope)]
   [:td (let [team (:user_team watch-scope)]
          [:> router/Link {:to (route/user-team-show (:id team))} (util.label/user-team-item team)])]
   [:td (util.watch-scope/render-terms (:terms watch-scope))]
   [:td
    [:> router/Link {:to (route/watch-scope-show (:id watch-scope))} util.label/show]
    " "
    [:> router/Link {:to (route/watch-scope-edit (:id watch-scope))} util.label/edit]
    " "
    [:f> util/btn-confirm-delete
     {:message-confirm (model.watch-scope/build-confirmation-message-for-deleting watch-scope)
      :action-delete #(model.watch-scope/delete {:id (:id watch-scope) :on-receive load-list})}]
    " "
    [:> router/Link {:to (route/watch-scope-device-files (:id watch-scope))} util.label/files]
    " "
    [:> router/Link {:to (route/watch-scope-device-logs (:id watch-scope))} util.label/logs]]])

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
