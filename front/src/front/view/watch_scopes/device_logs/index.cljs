(ns front.view.watch-scopes.device-logs.index
  (:require ["react-router-dom" :as router]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.device-log :as model.device-log]
            [front.view.util.device-log.page :as device-log.page]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id (get params "watch_scope_id")]
    (device-log.page/core (fn [params] (model.device-log/fetch-list-and-total-for-watch-scope
                                            (assoc params :id-watch-scope id))))))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
