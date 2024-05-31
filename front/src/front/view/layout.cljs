(ns front.view.layout
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [clojure.string :refer [includes?]]
            [lambdaisland.uri :as lamb.uri]
            [front.model.user :as user]
            [front.route :as route]
            [front.model.user :as model.user]))

(defn loader [js-params]
  (let [{:keys [request]} (-> js-params js->clj keywordize-keys)
        url (.-url request)
        uri (lamb.uri/uri url)
        path (:path uri)
        show-login-page-when-not-loggedin (and (includes? path "/front") (not (includes? path route/login)))]
  ; https://gist.github.com/pesterhazy/c4bab748214d2d59883e05339ce22a0f#asynchronous-conditionals
    (js/Promise.
     (fn [resolve _reject]
       (model.user/get-loggedin
        {:on-receive
         (fn [user]
           (if user
             (resolve user)
             (if show-login-page-when-not-loggedin
               (let [query (:query uri)
                     path-afetr-login (str path "?" query)
                     encoded-path-after-login (js/escape path-afetr-login)]
                 (resolve (router/redirect (str route/login "?path_after_login=" encoded-path-after-login))))
               (resolve nil))))})))))

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
          [:<>
           [:li.nav-item [:> router/Link {:to route/dashboard :class "nav-link"} "Dashboard"]]
           [:li.nav-item [:a {:on-click logout :class "nav-link"} "Logout"]]])]]]
     [:> router/Outlet]]))
