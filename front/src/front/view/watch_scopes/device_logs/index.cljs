(ns front.view.watch-scopes.device-logs.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.device-log :as model.device-log]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.device-log.page :as device-log.page]
            [front.view.util :as util]
            [front.view.watch-scopes.util :as v.watch-scope.util]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-watch-scope (get params "watch_scope_id")
        [item set-item] (react/useState)
        [errors set-errors] (react/useState)]
    (react/useEffect
     (fn []
       (model.watch-scope/fetch-by-id
        {:id id-watch-scope
         :on-receive (fn [item errors]
                       (set-errors errors)
                       (set-item item))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/watch-scopes) :path route/watch-scopes}
       {:label (util.label/watch-scope-item item) :path (route/watch-scope-show id-watch-scope)}
       {:label util.label/logs}]]
     (util/render-list-in-area-content-line
      (v.watch-scope.util/build-related-links item))
     [util/render-errors-as-alerts errors]
     (device-log.page/core
      (fn [params] (model.device-log/fetch-list-and-total-for-watch-scope
                    (assoc params :id-watch-scope id-watch-scope)))
      {:map-default {:str-renderer "[{\"key\":\"id\"},{\"key\":\"device_id\"},{\"key\":\"data\"},{\"key\":\"created_at\"}]"}})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
