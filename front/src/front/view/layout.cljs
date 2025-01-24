(ns front.view.layout
  (:require ["react-router-dom" :as router]
            ["react-bootstrap" :as bs]
            [clojure.walk :refer [keywordize-keys]]
            [lambdaisland.uri :as lamb.uri]
            [front.model.user :as model.user]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn loader [js-params]
  (let [{:keys [request]} (-> js-params js->clj keywordize-keys)
        url (.-url request)
        uri (lamb.uri/uri url)
        path (:path uri)
        show-login-page-when-not-loggedin (route/show-login-page-when-not-loggedin path)]
  ; https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f#asynchronous-conditionals
    (js/Promise.
     (fn [resolve _reject]
       (model.user/get-loggedin
        {:on-receive
         (fn [user]
           (if user
             (resolve user)
             (resolve nil)
             #_(if show-login-page-when-not-loggedin
                 (let [query (:query uri)
                       path-afetr-login (str path "?" query)
                       encoded-path-after-login (js/escape path-afetr-login)]
                   (resolve (router/redirect (str route/login "?path_after_login=" encoded-path-after-login))))
                 (resolve nil))))})))))

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
      [:> bs/Container {:fluid true}
       [:> bs/Navbar.Brand {:to "/" :as router/Link} "device logs"]
       [:> bs/Navbar.Toggle]
       [:> bs/Navbar.Collapse {:class :justify-content-end}
        [:> bs/Nav
         (if (nil? user)
           [:> bs/Nav.Link {:to route/login :as router/Link} "Login"]
           [:<>
            [:> bs/Nav.Link {:to route/dashboard :as router/Link} util.label/dashboard]
            [:> bs/NavDropdown {:title (:name user) :id "nav-dropdown" :align :end}
             [:> bs/NavDropdown.Item {:to route/profile :as router/Link} util.label/profile]
             [:> bs/NavDropdown.Divider]
             [:> bs/NavDropdown.Item {:on-click logout :href route/logout} util.label/logout]]])]]]]
     [:> router/Outlet]]))
