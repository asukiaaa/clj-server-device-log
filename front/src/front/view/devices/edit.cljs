(ns front.view.devices.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.device-type :as model.device-type]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.devices.util :as v.device.util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.util.user-team-device-config :as util.user-team-device-config]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "device_id")
        navigate (router/useNavigate)
        [device-type-list-and-total set-device-type-list-and-total] (react/useState)
        [user-team-list-and-total set-user-team-list-and-total] (react/useState)
        [item set-item] (react/useState)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-device-type-id (util/build-state-info :device_type_id #(react/useState))
        state-info-user-team-id (util/build-state-info :user_team_id #(react/useState))
        state-info-user-team-device-config-config (util/build-state-info :user_team_device_config_config react/useState)
        arr-state-info [state-info-name state-info-device-type-id state-info-user-team-id state-info-user-team-device-config-config]
        on-receive-item
        (fn [item]
          (set-item item)
          (doseq [state arr-state-info]
            (util/set-default-and-draft state ((:key state) item)))
          (util/set-default-and-draft state-info-user-team-device-config-config (-> item util.user-team-device-config/key-table :config)))
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
        fetch-device-types
        (fn [errors next]
          (model.device-type/fetch-list-and-total
           {:limit 1000
            :page 0
            :on-receive (fn [list-and-total new-errors]
                          (set-device-type-list-and-total list-and-total)
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
       (fetch-device-types
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
     [:f> breadcrumb/core [{:label (util.label/devices) :path route/devices}
                           {:label (util.label/device-item item)
                            :path (when item (route/device-show id-item))}
                           {:label (util.label/edit)}]]
     (util/render-list-in-area-content-line
      (v.device.util/build-related-links item {:id-item id-item}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div (util.label/no-data)]
         [:div
          [:form.form-control
           [util/render-input (util.label/name) state-info-name]
           [util/render-select (util.label/device-type) state-info-device-type-id
            (model.device-type/build-select-options-from-list-and-total device-type-list-and-total)]
           [util/render-select (util.label/user-team) state-info-user-team-id
            (model.user-team/build-select-options-from-list-and-total user-team-list-and-total)]
           [util/render-textarea util.label/config-on-user-team state-info-user-team-device-config-config
            {:disabled (empty? (str (:draft state-info-user-team-id)))}]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/edit)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
