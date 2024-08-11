(ns front.view.dashboard
  (:require ["react-router-dom" :as router]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.util :as util]))

(defn core []
  (let [user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)]
    [:div.list-group
     (when is-admin [:> router/Link {:to route/users :class "list-group-item list-group-item-action"} "users"])
     [:> router/Link {:to route/profile :class "list-group-item list-group-item-action"} "profile TODO"]]))
