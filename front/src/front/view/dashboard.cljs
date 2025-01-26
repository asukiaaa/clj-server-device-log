(ns front.view.dashboard
  (:require ["react-router-dom" :as router]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn page []
  (let [user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)]
    [:<>
     [:f> breadcrumb/core []]
     [:div.list-group
      (for [[name url] (remove nil? [(when is-admin [util.label/users route/users])
                                     [util.label/user-teams route/user-teams]
                                     ["device watch groups" route/device-watch-groups]
                                     [util.label/device-types route/device-types]
                                     [util.label/devices route/devices]
                                     [util.label/profile route/profile]])]
        [:<> {:key name}
         [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
