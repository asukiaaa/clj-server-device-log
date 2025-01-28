(ns front.view.dashboard
  (:require ["react-bootstrap" :as bs]
            ["react-router-dom" :as router]
            [front.model.device-file :as model.device-file]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]
            [front.view.util.device-file.page :as file.page]))

(defn page []
  (let [user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)]
    [:<>
     [:f> breadcrumb/core []]
     [:> bs/Container {:fluid true}
      [:> bs/Row
       [:> bs/Col {:sm 2 :class :px-0}
        [:div.list-group
         (for [[name url] (remove nil? [(when is-admin [util.label/users route/users])
                                        [util.label/user-teams route/user-teams]
                                        [util.label/watch-scopes route/watch-scopes]
                                        [util.label/device-types route/device-types]
                                        [util.label/devices route/devices]
                                        [util.label/profile route/profile]])]
           [:<> {:key name}
            [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]]
       [:> bs/Col {:class :px-0}
        [:f> file.page/core model.device-file/fetch-list-and-total-latest-each-device]]]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
