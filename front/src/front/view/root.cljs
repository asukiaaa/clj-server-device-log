(ns front.view.root
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util :as util]))

(defn core []
  (let [user-loggedin (util/get-user-loggedin)
        navigate (router/useNavigate)]
    (react/useEffect
     (fn []
       (navigate (if user-loggedin route/dashboard route/login))
       (fn []))
     #js [])
    [:div "redirect to top or login"]))
