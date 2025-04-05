(ns front.view.device-types.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "device_type_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-id-manager-user-team (util/build-state-info :manager_user_team_id #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-config-default (util/build-state-info :config_default react/useState)
        state-info-config-format (util/build-state-info :config_format react/useState)
        state-info-config-renderer-default (util/build-state-info :config_renderer_default react/useState)
        list-state-info [state-info-id-manager-user-team state-info-system state-info-name state-info-config-default state-info-config-format state-info-config-renderer-default]
        on-receive-item
        (fn [item]
          (set-item item)
          (doseq [state-info list-state-info]
            (util/set-default-and-draft state-info ((:key state-info) item))))
        on-receive-response (fn [data errors]
                              (when-not (empty? errors)
                                ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state list-state-info]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data model.device-type/key-table :id)]
                                  (navigate (route/device-type-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.device-type/update
           (reduce (fn [params state-info]
                     (assoc params (:key state-info) (:draft state-info)))
                   {:id id-item
                    :on-receive on-receive-response}
                   list-state-info)))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-type/fetch-by-id
        {:id id-item
         :on-receive (fn [user errors]
                       (on-receive-item user)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item item) :path (route/device-type-show id-item)}
       {:label (util.label/edit)}]]
     (util/render-list-in-area-content-line
      (v.device-type.util/build-related-links item {:id-item id-item}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div (util.label/no-data)]
         [:div
          [util/render-errors-as-alerts (:errors state-info-system)]
          [:form.form-control
           [util/render-input util.label/manager-user-team state-info-id-manager-user-team]
           [util/render-input (util.label/name) state-info-name]
           [util/render-textarea util.label/config-format state-info-config-format]
           [util/render-textarea util.label/config-default state-info-config-default]
           [util/render-textarea util.label/config-renderer-default state-info-config-renderer-default]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
