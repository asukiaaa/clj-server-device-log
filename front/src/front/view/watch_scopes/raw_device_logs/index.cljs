(ns front.view.watch-scopes.raw-device-logs.index
  (:require ["react-router-dom" :as router]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.raw-device-log :as model.raw-device-log]
            [front.view.util.raw-device-log.page :as raw-device-log.page]))

(defn-  page []
  (let [params (js->clj (router/useParams))
        id (get params "watch_scope_id")]
    (raw-device-log.page/core (fn [params] (model.raw-device-log/fetch-list-and-total-for-watch-scope
                                            (assoc params :id-watch-scope id))))))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
