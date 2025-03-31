(ns front.view.devices.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util :as util]
            [front.model.util.device :as util.device]
            [front.model.util.watch-scope :as util.watch-scope]))

(defn build-related-links [item]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (:id item)
        user (util/get-user-loggedin)
        is-admin (model.user/admin? user)]
    (->> [(util/build-link-or-text (util.label/show) (route/device-show id-item) path-current)
          (when is-admin (util/build-link-or-text (util.label/edit) (route/device-edit id-item) path-current))
          (util/build-link-or-text util.label/files (route/device-device-files id-item) path-current)
          (util/build-link-or-text util.label/logs (route/device-device-logs id-item) path-current)]
         (remove nil?))))

(defn render-active-watch-scope-terms [item & [{:keys [item-wrapper]}]]
  (let [item-wrapper (or item-wrapper :span.pe-1)]
    (for [term (util.device/key-active-watch-scope-terms item)]
      [item-wrapper {:key (:id term)}
       [:> router/Link {:to (route/watch-scope-show (:watch_scope_id term))}
        (util.label/watch-scope-item (util.watch-scope/key-table term))]])))
