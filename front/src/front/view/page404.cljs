(ns front.view.page404
  (:require
   ["react-router-dom" :as router]
   [front.route :as route]
   [front.view.util :as util]
   [front.view.util.label :as util.label]))

(defn core []
  (let [user (util/get-user-loggedin)
        location (router/useLocation)
        path-current (str (.-pathname location) (.-search location))
        url-object (new js/URL js/window.location.href)]
    [util/area-content
     (if user
       (util.label/page-not-found)
       [:> router/Link {:to route/login
                        :state {:path_after_login path-current}}
        (util.label/login-and-show-this-page)])]))
