(ns front.view.device-groups.device-group-api-keys.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-group-api-key :as model.device-group-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.device-group-api-key.explanation :as util.explanation]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-group (get params "id_device_group")
        navigate (router/useNavigate)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        state-info-system (util/build-state-info :__system #(react/useState))
        on-receive (fn [data]
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-name state-info-permission state-info-system]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data :device_group_api_key :id)]
                         (navigate (route/device-group-device-group-api-key-show id-device-group id)))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (model.device-group-api-key/create
                          {:name (:draft state-info-name)
                           :permission (:draft state-info-permission)
                           :id-device-group id-device-group
                           :on-receive on-receive}))]
    [:div
     [:h1.h3.mx-2 "create device group api key"]
     [:form.form-control
      [util/render-errors-as-alerts (:errors state-info-system)]
      [util/render-input "name" state-info-name]
      [util/render-textarea "permission" state-info-permission]
      [util.explanation/permission]
      [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
