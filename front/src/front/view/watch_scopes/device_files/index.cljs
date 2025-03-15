(ns front.view.watch-scopes.device-files.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-file :as model.device-file]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.device-file.page :as file.page]
            [front.view.util.label :as util.label]
            [front.view.watch-scopes.util :as v.watch-scope.util]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id-item (get params "watch_scope_id")
        [item set-item] (react/useState)
        on-receive
        (fn [result _errors]
          (set-item (:watch_scope result)))
        fetch-list-and-total
        (fn [params]
          (model.device-file/fetch-list-and-total-for-watch-scope
           (assoc params :id-watch-scope id-item)))]
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/watch-scopes :path route/watch-scopes}
       {:label (util.label/watch-scope-item item) :path (route/watch-scope-show id-item)}
       {:label util.label/files}]]
     (util/render-list-in-area-content-line
      (v.watch-scope.util/build-related-links item))
     [:f> file.page/core fetch-list-and-total {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
