(ns front.view.device-groups.device-group-api-keys.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-group-api-key :as model.device-group-api-key]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device-group (get params "id_device_group")
        id-device-group-api-key (get params "id_device_group_api_key")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-permission (:permission item)))
        on-receive-response (fn [data]
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-system state-info-name state-info-permission]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device_group_api_key :id)]
                                  (navigate (route/device-group-device-group-api-key-show id-device-group id)))))
        on-click-apply (fn [] (model.device-group-api-key/update
                               {:id id-device-group-api-key
                                :name (:draft state-info-name)
                                :permission (:draft state-info-permission)
                                :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-group-api-key/fetch-by-id-for-device-group
        {:id-device-group id-device-group
         :id-device-group-api-key id-device-group-api-key
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
         [:h1.h3.mx-2 "edit device group"]
         [:form.form-control
          [util/render-errors-as-alerts (:errors state-info-system)]
          [util/render-input "name" state-info-name]
          [util/render-input "permission" state-info-permission]
          [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
