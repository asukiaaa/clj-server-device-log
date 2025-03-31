(ns front.view.device-types.device-type-api-keys.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.model.device-type-api-key :as model.device-type-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.device-type-api-key.explanation :as util.explanation]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-type (get params "device_type_id")
        navigate (router/useNavigate)
        [device-type set-device-type] (react/useState)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        state-info-system (util/build-state-info :__system #(react/useState))
        on-receive
        (fn [data errors]
          (if-not (empty? errors)
            ((:set-errors state-info-system) errors)
            (if-let [errors-str (:errors data)]
              (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                (doseq [state [state-info-name state-info-permission state-info-system]]
                  (let [key (:key state)
                        errors-for-key (get errors key)]
                    ((:set-errors state) errors-for-key))))
              (when-let [id (-> data :device_type_api_key :id)]
                (navigate (route/device-type-device-type-api-key-show id-device-type id))))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.device-type-api-key/create
           {:name (:draft state-info-name)
            :permission (:draft state-info-permission)
            :id-device-type id-device-type
            :on-receive on-receive}))]
    (react/useEffect
     (fn []
       (model.device-type/fetch-by-id
        {:id id-device-type
         :on-receive (fn [item errors]
                       ((:set-errors state-info-system) errors)
                       (set-device-type item))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/device-types) :path route/device-types}
       {:label (util.label/device-type-item device-type) :path (route/device-type-show id-device-type)}
       {:label util.label/api-keys :path (route/device-type-device-type-api-keys id-device-type)}
       {:label (util.label/create)}]]
     [:form.form-control
      [util/render-errors-as-alerts (:errors state-info-system)]
      [util/render-input (util.label/name) state-info-name]
      [util/render-textarea util.label/permission state-info-permission]
      [util.explanation/permission]
      [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/create)]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
