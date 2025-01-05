(ns front.view.device-watch-groups.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-watch-group :as model.device-watch-group]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "id_device_watch_group")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-memo (util/build-state-info :memo #(react/useState))
        state-info-id-owner-user (util/build-state-info :owner_user_id #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-memo (:memo item))
          (util/set-default-and-draft state-info-id-owner-user (:owner_user_id item)))
        on-receive-response (fn [data errors]
                              (when errors ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-name state-info-memo state-info-id-owner-user]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data (get (keyword model.device-watch-group/name-table)) :id)]
                                  (navigate (route/device-watch-group-show id)))))
        on-click-apply (fn [] (model.device-watch-group/update
                               {:id id-item
                                :name (:draft state-info-name)
                                :memo (:draft state-info-memo)
                                :id-owner-user (:draft state-info-id-owner-user)
                                :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-watch-group/fetch-by-id {:id id-item
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
         [:h1.h3.mx-2 "edit device owner group"]
         [:form.form-control
          [util/render-errors-as-alerts (:errors state-info-system)]
          [util/render-input "name" state-info-name]
          [util/render-input "memo" state-info-memo]
          [util/render-input "owner user" state-info-id-owner-user]
          [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
