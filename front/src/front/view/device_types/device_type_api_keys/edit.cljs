(ns front.view.device-types.device-type-api-keys.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-type-api-key :as model.device-type-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.device-type-api-key.explanation :as util.explanation]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        id-device-type-api-key (get params "device_type_api_key_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        [device-type set-device-type] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-permission (:permission item)))
        on-receive-response (fn [data errors]
                              (when-not (empty? errors)
                                ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-system state-info-name state-info-permission]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device_type_api_key :id)]
                                  (navigate (route/device-type-device-type-api-key-show id-device-type id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (model.device-type-api-key/update
                          {:id id-device-type-api-key
                           :name (:draft state-info-name)
                           :permission (:draft state-info-permission)
                           :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-type-api-key/fetch-by-id-for-device-type
        {:id-device-type id-device-type
         :id-device-type-api-key id-device-type-api-key
         :on-receive (fn [user errors]
                       (on-receive-item user)
                       (set-device-type (model.device-type/key-table item))
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys :path (route/device-type-device-type-api-keys id-device-type)}
       {:label (util.label/api-key-item item) :path (route/device-type-device-type-api-key-show id-device-type id-device-type-api-key)}
       {:label (util.label/edit)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-input (util.label/name) state-info-name]
           [util/render-input util.label/permission state-info-permission]
           [util.explanation/permission]
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
