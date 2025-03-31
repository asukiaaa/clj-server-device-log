(ns front.view.watch-scopes.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (:id item)]
    [(util/build-link-or-text (util.label/show) (route/watch-scope-show id-item) path-current)
     (util/build-link-or-text (util.label/edit) (route/watch-scope-edit id-item) path-current)
     (util/build-link-or-text util.label/files (route/watch-scope-device-files id-item) path-current)
     (util/build-link-or-text util.label/logs (route/watch-scope-device-logs id-item) path-current)]))
