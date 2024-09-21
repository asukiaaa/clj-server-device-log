(ns front.view.devices.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.device-group :as model.device-group]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        [device-group-list-and-total set-device-group-list-and-total] (react/useState)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-device-group-id (util/build-state-info :name #(react/useState))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        limit-of-device-groups 10000 ; TODO apply search
        on-receive (fn [data]
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-name]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data :device :id)]
                         (navigate (route/device-show id)))))
        on-click-apply (fn [] (model.device/create
                               {:name (:draft state-info-name)
                                :device_group_id (:draft state-info-device-group-id)
                                :on-receive on-receive}))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.device-group/fetch-list-and-total
                     {:limit limit-of-device-groups
                      :page 0
                      :on-receive (fn [result errors]
                                    (set-device-group-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      [:div
       [:h1.h3.mx-2 "create device"]
       [:form.form-control
        [util/render-input "name" state-info-name]
        [util/render-select "device_group_id"
         state-info-device-group-id
         (model.device-group/build-select-options-from-list-and-total device-group-list-and-total)
         #_(for [item (:list list-and-total)]
             (let [id (:id item)
                   name (:name item)]
               [id (str id " " name)]))]
        [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
