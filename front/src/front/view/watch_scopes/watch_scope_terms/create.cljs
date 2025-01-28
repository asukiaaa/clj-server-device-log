(ns front.view.watch-scopes.watch-scope-terms.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.watch-scope-term :as model.watch-scope-term]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-watch-scope (get params "watch_scope_id")
        navigate (router/useNavigate)
        state-info-display-name (util/build-state-info :name #(react/useState))
        state-info-id-device (util/build-state-info :permission #(react/useState))
        state-info-system (util/build-state-info :__system #(react/useState))
        on-receive (fn [data errors]
                     (when-not (empty? errors) (js/alert errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-display-name state-info-id-device state-info-system]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data :watch_scope_term :id)]
                         (navigate (route/watch-scope-watch-scope-term-show id-watch-scope id)))))
        on-click-apply (fn [] (model.watch-scope-term/create
                               {:display-name (:draft state-info-display-name)
                                :id-device (:draft state-info-id-device)
                                :id-watch-scope id-watch-scope
                                :on-receive on-receive}))]
    [:div
     [:h1.h3.mx-2 "create device watch group device"]
     [:form.form-control
      [util/render-errors-as-alerts (:errors state-info-system)]
      [:div
       [:div "watch_scope_id"]
       [:div id-watch-scope]]
      [util/render-input "display name" state-info-display-name]
      [util/render-input "device id" state-info-id-device]
      [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
