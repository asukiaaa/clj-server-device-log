(ns front.view.user-teams.members.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user-team :as model.user-team]
            [front.model.user-team-member :as model.user-team-member]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-user-team (get params "user_team_id")
        navigate (router/useNavigate)
        [user-team set-user-team] (react/useState)
        [waiting-response set-waiting-response] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-user-email (util/build-state-info :user_email #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        arr-state-info [state-info-system state-info-permission state-info-user-email]
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive (fn [data errors]
                     (set-waiting-response false)
                     (when errors ((:set-errors state-info-system) errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state arr-state-info]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data (get (keyword model.user-team-member/name-table)) :id)]
                         (navigate (route/user-team-member-show id-user-team id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (set-waiting-response true)
                         (model.user-team-member/create
                          (util/assign-arr-state-info-to-params
                           arr-state-info
                           {:user_team_id id-user-team
                            :on-receive on-receive})))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team/fetch-by-id
        {:id id-user-team
         :on-receive (fn [item errors]
                       (set-user-team item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/user-teams :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label util.label/members :path (route/user-team-members id-user-team)}
       {:label util.label/create}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input "user email" state-info-user-email {:disabled waiting-response}]
         [util/render-textarea "permission" state-info-permission {:disabled waiting-response}]
         [:button.btn.btn-primary.mt-1 {:on-click on-click-apply :disabled waiting-response} util.label/create]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
