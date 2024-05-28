(ns front.view.dashboard
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.model.user :as user]
            [front.route :as route]))

(defn core []
  (let [navigate (router/useNavigate)
        logout #(navigate route/login)]
    [:div.list-group
     [:> router/Link {:to route/users :class "list-group-item list-group-item-action"} "users"]]))
