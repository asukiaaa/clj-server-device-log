(ns front.view.user-teams.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user-team :as model.user-team]
            [front.model.user :as model.user]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        [waiting-response set-waiting-response] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-memo (util/build-state-info :memo #(react/useState))
        state-info-id-owner-user (util/build-state-info :owner_user_id #(react/useState))
        [user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive (fn [data errors]
                     (set-waiting-response false)
                     (when errors ((:set-errors state-info-system) errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-system state-info-name state-info-memo state-info-id-owner-user]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data (get (keyword model.user-team/name-table)) :id)]
                         (navigate (route/user-team-show id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (set-waiting-response true)
                         (model.user-team/create
                          {:name (:draft state-info-name)
                           :memo (:draft state-info-memo)
                           :owner_user_id (:draft state-info-id-owner-user)
                           :on-receive on-receive}))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.user/fetch-list-and-total
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
     [:f> breadcrumb/core [{:label (util.label/user-teams) :path route/user-teams}
                           {:label (util.label/create)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input (util.label/name) state-info-name {:disabled waiting-response}]
         [util/render-textarea util.label/memo state-info-memo {:disabled waiting-response}]
         [util/render-select util.label/owner-user state-info-id-owner-user (model.user/build-select-options-from-list-and-total user-list-and-total) {:disabled waiting-response}]
         [:button.btn.btn-primary.mt-1 {:on-click on-click-apply :disabled waiting-response} (util.label/create)]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
