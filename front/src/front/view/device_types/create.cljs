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
        state-info-name (util/build-state-info :name #(react/useState))
        on-receive (fn [data]
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-name]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data model.device-type/key-table :id)]
                         (navigate (route/device-type-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.device-type/create
           {:name (:draft state-info-name)
            :on-receive on-receive}))]
    [:div
     [:f> breadcrumb/core [{:label util.label/device-types :path route/device-types}
                           {:label util.label/create}]]
     [:form.form-control
      [util/render-input "name" state-info-name]
      [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/create]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
