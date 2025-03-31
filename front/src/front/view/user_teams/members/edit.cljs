(ns front.view.user-teams.members.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user-team :as model.user-team]
            [front.model.user-team-member :as model.user-team-member]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "user_team_member_id")
        id-user-team (get params "user_team_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        [user-team set-user-team] (react/useState)
        [waiting-response set-waiting-response] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (set-user-team (model.user-team/key-table item))
          (util/set-default-and-draft state-info-permission (:permission item)))
        on-receive-response (fn [data errors]
                              (set-waiting-response false)
                              (when errors ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-permission]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data (get (keyword model.user-team-member/name-table)) :id)]
                                  (navigate (route/user-team-member-show id-user-team id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (set-waiting-response true)
                         (model.user-team-member/update
                          {:id id-item
                           :permission (:draft state-info-permission)
                           :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team-member/fetch-by-id-for-user-team
        {:id id-item
         :user_team_id id-user-team
         :on-receive (fn [user errors]
                       (on-receive-item user)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (util.label/user-team-item user-team) :path (route/user-team-show id-user-team)}
       {:label util.label/members :path (route/user-team-members id-user-team)}
       {:label (util.label/user-team-member-item item) :path (route/user-team-member-show id-user-team id-item)}
       {:label (util.label/edit)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-textarea util.label/permission state-info-permission {:disabled waiting-response}]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply :disabled waiting-response} (util.label/edit)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
