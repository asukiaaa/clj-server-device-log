(ns front.view.devices.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.device-group :as model.device-group]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "device_id")
        navigate (router/useNavigate)
        [device-group-list-and-total set-device-group-list-and-total] (react/useState)
        [user-team-list-and-total set-user-team-list-and-total] (react/useState)
        [item set-item] (react/useState)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-device-group-id (util/build-state-info :device_group_id #(react/useState))
        state-info-user-team-id (util/build-state-info :user_team_id #(react/useState))
        arr-state-info [state-info-name state-info-device-group-id state-info-user-team-id]
        on-receive-item
        (fn [item]
          (set-item item)
          (doseq [state arr-state-info]
            (util/set-default-and-draft state ((:key state) item))))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive-response (fn [data errors]
                              (wrapper.fetching/set-errors info-wrapper-fetching errors)
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state arr-state-info]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device :id)]
                                  (navigate (route/device-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (let [params (reduce (fn [params state]
                                 (assoc params (:key state) (:draft state)))
                               nil arr-state-info)]
            (model.device/update
             (merge params
                    {:id id-item
                     :on-receive on-receive-response}))))
        fetch-user-teams
        (fn [errors next]
          (model.user-team/fetch-list-and-total
           {:limit 1000
            :page 0
            :on-receive (fn [list-and-total new-errors]
                          (set-user-team-list-and-total list-and-total)
                          (next (concat errors new-errors)))}))
        fetch-device-groups
        (fn [errors next]
          (model.device-group/fetch-list-and-total
           {:limit 1000
            :page 0
            :on-receive (fn [list-and-total new-errors]
                          (set-device-group-list-and-total list-and-total)
                          (next (concat errors new-errors)))}))
        fetch-device
        (fn [errors next]
          (model.device/fetch-by-id
           {:id id-item
            :on-receive (fn [user new-errors]
                          (on-receive-item user)
                          (next (concat errors new-errors)))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (fetch-device-groups
        nil
        (fn [errors]
          (fetch-device
           errors
           (fn [errors]
             (fetch-user-teams
              errors
              (fn [errors]
                (wrapper.fetching/finished info-wrapper-fetching errors)))))))
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/devices :path route/devices}
                           {:label (util.label/device item)
                            :path (when item (route/device-show id-item))}
                           {:label util.label/edit}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:form.form-control
           [util/render-input "name" state-info-name]
           [util/render-select "device group id" state-info-device-group-id
            (model.device-group/build-select-options-from-list-and-total device-group-list-and-total)]
           [util/render-select "user team id" state-info-user-team-id
            (model.user-team/build-select-options-from-list-and-total user-team-list-and-total)]
           [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
