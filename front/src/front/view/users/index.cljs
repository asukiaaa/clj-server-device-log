(ns front.view.users.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.user :as model.user]
            [front.view.users.util :as v.user.util]
            [front.view.util.table :as util.table]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn- page []
  (let [labels-header [(util.label/name) (util.label/email) (util.label/action)]
        user-loggedin (util/get-user-loggedin)
        render-user
        (fn [user load-list]
          [:tr
           [:td (:name user)]
           [:td (:email user)]
           [:td
            (util/render-list-inline
             (v.user.util/build-related-links user))]])]
    [:<>
     [:f> breadcrumb/core [{:label (util.label/users)}]]
     (when (model.user/admin? user-loggedin)
       [util/area-content
        [:> router/Link {:to route/user-create} (util.label/create)]])
     [:f> util.table/core model.user/fetch-list-and-total labels-header render-user]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
