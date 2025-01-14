(ns front.view.dashboard
  (:require ["react-router-dom" :as router]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.util :as util]))

(defn core []
  (let [user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)]
    [:div.list-group
     (for [[name url] (remove nil? [(when is-admin ["users" route/users])
                                    ["device watch groups" route/device-watch-groups]
                                    ["device groups" route/device-groups]
                                    ["devices" route/devices]
                                    #_["profile" route/profile]])]
       [:<> {:key name}
        [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]))
