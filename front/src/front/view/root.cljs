(ns front.view.root
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [goog.string :refer [format]]
            [front.route :as route]
            [front.view.util :as util]))

(defn core []
  (let [user-loggedin (util/get-user-loggedin)
        navigate (router/useNavigate)]
    (react/useEffect
     (fn []
       (navigate (if user-loggedin route/home route/login))
       (fn []))
     #js [])
    [:div (format "redirect to %s or %s" route/home route/login)]))
