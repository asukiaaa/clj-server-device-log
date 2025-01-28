(ns front.view.watch-scopes.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-id-user-team (util/build-state-info :user_team_id #(react/useState))
        [user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive (fn [data errors]
                     (when errors ((:set-errors state-info-system) errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-system state-info-name state-info-id-user-team]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data (get (keyword model.watch-scope/name-table)) :id)]
                         (navigate (route/watch-scope-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.watch-scope/create
           {:name (:draft state-info-name)
            :user_team_id (:draft state-info-id-user-team)
            :on-receive on-receive}))
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.user-team/fetch-list-and-total
           {:limit 10000 ; TODO apply search
            :page 0
            :on-receive (fn [result errors]
                          (set-user-list-and-total result)
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/watch-scopes :path route/watch-scopes}
                           {:label util.label/create}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input util.label/name state-info-name]
         [util/render-select util.label/user-team state-info-id-user-team (model.user-team/build-select-options-from-list-and-total user-list-and-total)]
         [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/create]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
