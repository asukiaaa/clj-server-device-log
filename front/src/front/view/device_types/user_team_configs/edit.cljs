(ns front.view.device-types.user-team-configs.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.user-team :as model.user-team]
            [front.model.user-team-device-type-config :as model.user-team-device-type-config]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        id-user-team (get params "user_team_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        [device-type set-device-type] (react/useState)
        [user-team set-user-team] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-config (util/build-state-info :config #(react/useState))
        arr-state-info [state-info-system state-info-config]
        on-receive-item
        (fn [item]
          (set-item item)
          (set-device-type (model.device-type/key-table item))
          (set-user-team (model.user-team/key-table item))
          (doseq [state-info arr-state-info]
            (util/set-default-and-draft state-info ((:key state-info) item))))
        on-receive-response (fn [data errors]
                              (when errors
                                ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state arr-state-info]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when (-> data model.user-team-device-type-config/key-table :id)
                                  (navigate (route/device-type-user-team-config-show id-device-type id-user-team)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (model.user-team-device-type-config/update
                          (util/assign-arr-state-info-to-params
                           arr-state-info
                           {:user_team_id id-user-team
                            :device_type_id id-device-type
                            :on-receive on-receive-response})))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team-device-type-config/fetch-by-user-team-and-device-type
        {:device_type_id id-device-type
         :user_team_id id-user-team
         :to-edit true
         :on-receive (fn [item errors]
                       (on-receive-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label (util.label/user-team-configs) :path (route/device-type-user-team-configs id-device-type)}
       {:label (util.label/user-team-item user-team) :path (route/device-type-user-team-config-show id-device-type id-user-team)}
       {:label (util.label/edit)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [util/area-content "no data"]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-input (util.label/device-type-config-on-user-team) state-info-config]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
