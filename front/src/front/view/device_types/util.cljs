(ns front.view.device-types.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item & [{:keys [id-item]}]]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (or id-item (:id item))
        is-admin (util/detect-is-admin-loggedin)]
    (->> [(util/build-link-or-text (util.label/show) (route/device-type-show id-item) path-current)
          (when is-admin (util/build-link-or-text (util.label/edit) (route/device-type-edit id-item) path-current))
          (when is-admin (util/build-link-or-text util.label/api-keys (route/device-type-device-type-api-keys id-item) path-current))
          (util/build-link-or-text util.label/logs (route/device-type-device-logs id-item) path-current)
          (util/build-link-or-text (util.label/user-team-configs) (route/device-type-user-team-configs id-item) path-current)]
         (remove nil?))))
