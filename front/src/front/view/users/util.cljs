(ns front.view.users.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item & [{:keys [id-item]}]]
  (let [id-user (or id-item (:id item))
        location (router/useLocation)
        path-current (.-pathname location)
        user (util/get-user-loggedin)
        is-admin (model.user/admin? user)]
    (->> [(util/build-link-or-text (util.label/show) (route/user-show id-user) path-current)
          (when is-admin
            (util/build-link-or-text (util.label/edit) (route/user-edit id-user) path-current))]
         (remove nil?))))
