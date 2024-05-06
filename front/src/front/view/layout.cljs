(ns front.view.layout
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.model.user :as user]
            [front.route :as route]
            [front.model.user :as model.user]))

(defn core []
  (let [location (router/useLocation)
        navigate (router/useNavigate)
        [user set-user] (react/useState nil)
        logout (fn [] (model.user/logout {:on-receive #(navigate route/login)}))]
    (react/useEffect
     (fn [] (user/get-loggedin {:on-receive #(set-user %)})
       (fn []))
     (clj->js [location]))
    [:div
     [:nav.navbar.bg-body-tertiary.border-bottom
      [:div.container-fluid
       [:> router/Link {:to "/" :class "navbar-brand text-dark"} "device logs"]
       [:ul.nav.justify-content-end
        (if (nil? user)
          [:li.nav-item [:> router/Link {:to route/login :class "nav-link"} "Login"]]
          [:li.nav-item [:a {:on-click logout :class "nav-link"} "Logout"]])]]]
     [:> router/Outlet]]))
