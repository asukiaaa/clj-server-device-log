(ns front.view.device-watch-groups.device-watch-group-devices.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-watch-group-device :as model.device-watch-group-device]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-watch-group (get params "id_device_watch_group")
        navigate (router/useNavigate)
        state-info-display-name (util/build-state-info :name #(react/useState))
        state-info-id-device (util/build-state-info :permission #(react/useState))
        state-info-system (util/build-state-info :__system #(react/useState))
        on-receive (fn [data errors]
                     (when-not (empty? errors) (js/alert errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-display-name state-info-id-device state-info-system]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data :device_watch_group_device :id)]
                         (navigate (route/device-watch-group-device-watch-group-device-show id-device-watch-group id)))))
        on-click-apply (fn [] (model.device-watch-group-device/create
                               {:display-name (:draft state-info-display-name)
                                :id-device (:draft state-info-id-device)
                                :id-device-watch-group id-device-watch-group
                                :on-receive on-receive}))]
    [:div
     [:h1.h3.mx-2 "create device watch group device"]
     [:form.form-control
      [util/render-errors-as-alerts (:errors state-info-system)]
      [:div
       [:div "device_watch_group_id"]
       [:div id-device-watch-group]]
      [util/render-input "device name" state-info-display-name]
      [util/render-input "device id" state-info-id-device]
      [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
