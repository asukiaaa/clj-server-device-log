(ns front.view.device-watch-groups.device-watch-group-devices.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-watch-group-device :as model.device-watch-group-device]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-watch-group (get params "id_device_watch_group")
        id-device-watch-group-device (get params "id_device_watch_group_device")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name-device (util/build-state-info :name #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name-device (:name item)))
        on-receive-response (fn [data]
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-system state-info-name-device]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device_watch_group_device :id)]
                                  (navigate (route/device-watch-group-device-watch-group-device-show id-device-watch-group id)))))
        on-click-apply (fn [] (model.device-watch-group-device/update
                               {:id id-device-watch-group-device
                                :name-device (:draft state-info-name-device)
                                :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-watch-group-device/fetch-by-id-for-device-watch-group
        {:id-device-watch-group id-device-watch-group
         :id id-device-watch-group-device
         :on-receive (fn [user errors]
                       (on-receive-item user)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:h1.h3.mx-2 "edit device watch group device"]
         [:form.form-control
          [util/render-errors-as-alerts (:errors state-info-system)]
          [util/render-input "name device" state-info-name-device]
          [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
