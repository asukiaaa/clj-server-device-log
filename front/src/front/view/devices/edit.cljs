(ns front.view.devices.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.model.device-group :as model.device-group]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "id_device")
        navigate (router/useNavigate)
        [device-group-list-and-total set-device-group-list-and-total] (react/useState)
        [item set-item] (react/useState)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-device-group-id (util/build-state-info :device_group_id #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-device-group-id (:device_group_id item)))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive-response (fn [data errors]
                              (wrapper.fetching/set-errors info-wrapper-fetching errors)
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-name state-info-device-group-id]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device :id)]
                                  (navigate (route/device-show id)))))
        on-click-apply (fn []
                         (model.device/update
                          {:id id-item
                           :name (:draft state-info-name)
                           :device_group_id (:draft state-info-device-group-id)
                           :on-receive on-receive-response}))
        fetch-device-groups (fn [errors next]
                              (model.device-group/fetch-list-and-total
                               {:on-receive (fn [list-and-total new-errors]
                                              (set-device-group-list-and-total list-and-total)
                                              (next (concat errors new-errors)))}))
        fetch-device (fn [errors next]
                       (model.device/fetch-by-id
                        {:id id-item
                         :on-receive (fn [user new-errors]
                                       (on-receive-item user)
                                       (next (concat errors new-errors)))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (fetch-device-groups nil (fn [errors] (fetch-device errors (fn [errors] (wrapper.fetching/finished info-wrapper-fetching errors)))))
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:h1.h3.mx-2 "edit device"]
         [:form.form-control
          [util/render-input "name" state-info-name]
          [util/render-select "device_group_id" state-info-device-group-id (model.device-group/build-select-options-from-list-and-total device-group-list-and-total)]
          [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
