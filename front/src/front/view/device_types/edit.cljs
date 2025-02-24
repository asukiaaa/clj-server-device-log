(ns front.view.device-types.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "device_type_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-config-default (util/build-state-info :config_default react/useState)
        state-info-config-format (util/build-state-info :config_format react/useState)
        list-state-info [state-info-system state-info-name state-info-config-default state-info-config-format]
        on-receive-item
        (fn [item]
          (set-item item)
          (doseq [state-info list-state-info]
            (util/set-default-and-draft state-info ((:key state-info) item))))
        on-receive-response (fn [data]
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
       (model.device-type/fetch-by-id {:id id-item
                                       :on-receive (fn [user errors]
                                                     (on-receive-item user)
                                                     (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/device-types :path route/device-types}
       {:label (util.label/device-type-item item) :path (route/device-type-show id-item)}
       {:label util.label/edit}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div util.label/no-data]
         [:div
          [:form.form-control
           [util/render-input util.label/name state-info-name]
           [util/render-textarea util.label/config-format state-info-config-format]
           [util/render-textarea util.label/config-default state-info-config-default]
           [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/edit]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
