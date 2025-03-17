(ns front.view.layout
  (:require ["react-router-dom" :as router]
            ["react-bootstrap" :as bs]
            [clojure.walk :refer [keywordize-keys]]
            [lambdaisland.uri :as lamb.uri]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util.links :as util.links]
            [front.view.util :as util]))

(defn loader [js-params]
  (js/Promise.
   (fn [resolve _reject]
     (model.user/get-loggedin
      {:on-receive
       (fn [{:keys [user errors]}]
         (if (or user errors)
           (if user
             (resolve user)
             (resolve nil))
           (resolve nil)))}))))

(defn core []
  (let [navigate (router/useNavigate)
        revalidator (router/useRevalidator)
        user (router/useRouteLoaderData util/key-user-loggedin)
        logout (fn [e]
                 (.preventDefault e)
                 (model.user/logout {:on-receive (fn []
                                                   (.revalidate revalidator)
                                                   (navigate route/login))}))]
    [:div
     [:> bs/Navbar {:bg :light :data-bs-theme :light}
      [:> bs/Container {:fluid true :style {:display :block :text-align-last :justify}}
       [:> bs/Navbar.Brand {:to "/" :as router/Link :style {:display :inline-block}} "Device log"]
       [:span " "]
       [:> bs/Nav {:style {:display :inline-block}}
        (if (nil? user)
          [:> bs/Nav.Link {:to route/login :as router/Link} "Login"]
          [:<>
           [:> bs/Nav.Link {:to route/dashboard :as router/Link :style {:display :inline-block}} util.label/dashboard]
           [:> bs/NavDropdown {:title (:name user) :id "nav-dropdown" :align :end :style {:display :inline-block :text-align-last :start}}
            (for [[label path] (util.links/build-list-menu-links-for-user user)]
              [:<> {:key label}
               [:> bs/NavDropdown.Item {:to path :as router/Link :key path} label]])
            [:> bs/NavDropdown.Divider]
            [:> bs/NavDropdown.Item {:on-click logout :href route/logout} util.label/logout]]])]]]
     [:> router/Outlet]]))
