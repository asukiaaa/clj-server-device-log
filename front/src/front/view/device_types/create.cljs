(ns front.view.device-types.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-name (util/build-state-info :name react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-config-default (util/build-state-info :config_default react/useState)
        state-info-config-format (util/build-state-info :config_format react/useState)
        list-state-info [state-info-system state-info-name state-info-config-default state-info-config-format]
        on-receive (fn [data]
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
          (model.device-type/create
           (reduce (fn [params state-info]
                     (assoc params (:key state-info) (:draft state-info)))
                   {:on-receive on-receive}
                   list-state-info)))]
    [:div
     [:f> breadcrumb/core [{:label util.label/device-types :path route/device-types}
                           {:label util.label/create}]]
     [:form.form-control
      [util/render-input util.label/name state-info-name]
      [util/render-textarea util.label/config-format state-info-config-format]
      [util/render-textarea util.label/config-default state-info-config-default]
      [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/create]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
