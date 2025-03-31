(ns front.view.user-teams.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.user-teams.util :as v.team.util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "user_team_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        [waiting-response set-waiting-response] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-memo (util/build-state-info :memo #(react/useState))
        state-info-id-owner-user (util/build-state-info :owner_user_id #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-memo (:memo item))
          (util/set-default-and-draft state-info-id-owner-user (:owner_user_id item)))
        on-receive-response (fn [data errors]
                              (set-waiting-response false)
                              (when errors ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-name state-info-memo state-info-id-owner-user]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data (get (keyword model.user-team/name-table)) :id)]
                                  (navigate (route/user-team-show id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (set-waiting-response true)
                         (model.user-team/update
                          {:id id-item
                           :name (:draft state-info-name)
                           :memo (:draft state-info-memo)
                           :owner_user_id (:draft state-info-id-owner-user)
                           :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team/fetch-by-id {:id id-item
                                     :on-receive (fn [user errors]
                                                   (on-receive-item user)
                                                   (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (or (:name item) util.label/no-data) :path (route/user-team-show id-item)}
       {:label (util.label/edit)}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links item {:id-item id-item}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div (util.label/no-data)]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-input (util.label/name) state-info-name {:disabled waiting-response}]
           [util/render-input util.label/memo state-info-memo {:disabled waiting-response}]
           [util/render-input util.label/owner-user state-info-id-owner-user {:disabled waiting-response}]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply :disabled waiting-response} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
