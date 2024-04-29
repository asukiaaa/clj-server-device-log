(ns front.view.dashboard
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.model.user :as user]
            [front.route :as route]))

(defn core []
  (let [navigate (router/useNavigate)
        logout #(navigate route/login)]
    [:div "dashboard TODO"
     [:div "hoge"]
     [:a {:on-click #(user/logout {:on-receive logout})} "logout"]]))
