(ns front.view.user-teams.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (:id item)
        is-admin (util/detect-is-admin-loggedin)]
    (->> [(util/build-link-or-text util.label/show (route/user-team-show id-item) path-current)
          (when is-admin (util/build-link-or-text util.label/edit (route/user-team-edit id-item) path-current))
          (util/build-link-or-text util.label/member (route/user-team-members id-item) path-current)
          (util/build-link-or-text util.label/device-type (route/user-team-device-types id-item) path-current)]
         (remove nil?))))
