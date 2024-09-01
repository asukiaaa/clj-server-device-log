(ns front.view.device-groups.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-group :as model.device-group]
            [front.view.common.wrapper.show404 :as wrapper.show404]
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
                       (when-let [id (-> data :device_group :id)]
                         (navigate (route/device-group-show id)))))
        on-click-apply (fn [] (model.device-group/create
                               {:name (:draft state-info-name)
                                :on-receive on-receive}))]
    [:div
     [:h1.h3.mx-2 "create device group"]
     [:form.form-control
      [util/render-input "name" state-info-name]
      [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
