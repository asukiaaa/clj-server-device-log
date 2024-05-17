(ns front.view.layout
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.model.user :as user]
            [front.route :as route]
            [front.model.user :as model.user]))

(defn loader []
  ; https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f#asynchronous-conditionals
  (js/Promise.
   (fn [resolve _reject]
     (model.user/get-loggedin {:on-receive #(resolve %)}))))

(defn core []
  (let [navigate (router/useNavigate)
        revalidator (router/useRevalidator)
        user (router/useRouteLoaderData "user-loggedin")
        logout (fn [] (model.user/logout {:on-receive (fn []
                                                        (.revalidate revalidator)
                                                        (navigate route/login))}))]
    [:div
     [:nav.navbar.bg-body-tertiary.border-bottom
      [:div.container-fluid
       [:> router/Link {:to "/" :class "navbar-brand text-dark"} "device logs"]
       [:ul.nav.justify-content-end
        (if (nil? user)
          [:li.nav-item [:> router/Link {:to route/login :class "nav-link"} "Login"]]
          [:li.nav-item [:a {:on-click logout :class "nav-link"} "Logout"]])]]]
     [:> router/Outlet]]))
