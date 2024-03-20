(ns front.view.layout
  (:require ["react-router-dom" :as router]))

(defn core []
  [:div
   [:nav.navbar.bg-body-tertiary.border-bottom
    [:div.container-fluid
     [:> router/Link {:to "/" :class "navbar-brand text-dark"} "device logs"]
     [:ul.nav.justify-content-end
      [:li.nav-item [:> router/Link {:to "/front/login" :class "nav-link"} "Login"]]]]]
   [:> router/Outlet]])
