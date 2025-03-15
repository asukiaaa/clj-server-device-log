(ns front.view.devices.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (:id item)
        user (util/get-user-loggedin)
        is-admin (model.user/admin? user)]
    (->> [(util/build-link-or-text util.label/show (route/device-show id-item) path-current)
          (when is-admin (util/build-link-or-text util.label/edit (route/device-edit id-item) path-current))
          (util/build-link-or-text util.label/files (route/device-device-files id-item) path-current)
          (util/build-link-or-text util.label/logs (route/device-device-logs id-item) path-current)]
         (remove nil?))))
