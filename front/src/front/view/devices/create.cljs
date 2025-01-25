(ns front.view.devices.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.device-group :as model.device-group]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        [waiting-response set-waiting-response] (react/useState)
        [device-group-list-and-total set-device-group-list-and-total] (react/useState)
        [user-team-list-and-total set-user-team-list-and-total] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-device-group-id (util/build-state-info :device_group_id #(react/useState))
        state-info-user-team-id (util/build-state-info :user_team_id #(react/useState))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        limit-of-list 1000 ; TODO apply search
        arr-state-info [state-info-system state-info-device-group-id state-info-user-team-id]
        on-receive (fn [data errors]
                     (set-waiting-response false)
                     (if-not (empty? errors)
                       ((:set-errors state-info-system) errors)
                       (if-let [errors-str (:errors data)]
                         (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                           (doseq [state arr-state-info]
                             (let [key (:key state)
                                   errors-for-key (get errors key)]
                               ((:set-errors state) errors-for-key))))
                         (when-let [id (-> data :device :id)]
                           (navigate (route/device-show id))))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (set-waiting-response true)
          (let [params (reduce (fn [params state]
                                 (assoc params (:key state) (:draft state)))
                               nil arr-state-info)]
            (model.device/create
             (merge params
                    {:on-receive on-receive}))))
        fetch-user-teams
        (fn [errors next]
          (model.user-team/fetch-list-and-total
           {:limit limit-of-list
            :page 0
            :on-receive (fn [list-and-total new-errors]
                          (set-user-team-list-and-total list-and-total)
                          (next (concat errors new-errors)))}))
        fetch-device-groups
        (fn [errors next]

          (model.device-group/fetch-list-and-total
           {:limit limit-of-list
            :page 0
            :on-receive (fn [result new-errors]
                          (set-device-group-list-and-total result)
                          (next (concat errors new-errors)))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       ; TODO use promise
       (fetch-device-groups
        nil
        (fn [errors]
          (fetch-user-teams
           errors
           (fn [errors]
             (wrapper.fetching/finished info-wrapper-fetching errors)))))
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/devices :path route/devices}
       {:label util.label/create}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input "name" state-info-name]
         [util/render-select "device_group_id"
          state-info-device-group-id
          (model.device-group/build-select-options-from-list-and-total device-group-list-and-total)]
         [util/render-select "user team id" state-info-user-team-id
          (model.user-team/build-select-options-from-list-and-total user-team-list-and-total)]
         [:button.btn.btn-primary.btn-sm.mt-1
          {:on-click on-click-apply
           :disabled waiting-response}
          util.label/create]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
