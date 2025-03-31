(ns front.view.watch-scopes.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.user-team :as model.user-team]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-id-user-team (util/build-state-info :user_team_id #(react/useState))
        state-info-terms (util/build-state-info :terms react/useState {:default (util.watch-scope/build-initial-terms-params)})
        [device-list-and-total set-device-list-and-total] (react/useState)
        [user-team-list-and-total set-user-team-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive
        (fn [data errors]
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
            :terms (util.watch-scope/terms-draft->params (:draft state-info-terms))
            :user_team_id (:draft state-info-id-user-team)
            :on-receive on-receive}))
        load-devices
        (fn [& [{:keys [user_team_id]}]]
          (set-device-list-and-total nil)
          (model.device/fetch-list-and-total-for-user-team
           {:limit 1000
            :user_team_id (if user_team_id user_team_id (:draft state-info-id-user-team))
            :page 0
            :on-receive
            (fn [result errors]
              (set-device-list-and-total result))}))
        load-user-teams
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.user-team/fetch-list-and-total
           {:limit 10000 ; TODO apply search
            :page 0
            :on-receive (fn [result errors]
                          (set-user-team-list-and-total result)
                          (let [list (:list result)]
                            (when (= 1 (count list))
                              (let [id-user-team (-> list first :id)]
                                (util/set-default-and-draft state-info-id-user-team id-user-team)
                                (load-devices {:user_team_id id-user-team}))))
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-user-teams)
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/watch-scopes) :path route/watch-scopes}
                           {:label (util.label/create)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input (util.label/name) state-info-name]
         [util/render-select (util.label/user-team) state-info-id-user-team
          (model.user-team/build-select-options-from-list-and-total user-team-list-and-total)
          {:on-blur load-devices}]
         (util.watch-scope/render-fields-for-terms state-info-terms device-list-and-total)
         [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/create)]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
