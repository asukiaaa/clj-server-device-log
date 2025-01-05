(ns front.view.device-watch-groups.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device-watch-group :as model.device-watch-group]
            [front.model.user :as model.user]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-memo (util/build-state-info :memo #(react/useState))
        state-info-id-owner-user (util/build-state-info :owner_user_id #(react/useState))
        [user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive (fn [data errors]
                     (when errors ((:set-errors state-info-system) errors))
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-system state-info-name state-info-memo state-info-id-owner-user]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id (-> data (get (keyword model.device-watch-group/name-table)) :id)]
                         (navigate (route/device-watch-group-show id)))))
        on-click-apply (fn [] (model.device-watch-group/create
                               {:name (:draft state-info-name)
                                :memo (:draft state-info-memo)
                                :id-owner-user (:draft state-info-id-owner-user)
                                :on-receive on-receive}))
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.user/fetch-list-and-total
                     {:limit 10000 ; TODO apply search
                      :page 0
                      :on-receive (fn [result errors]
                                    (set-user-list-and-total result)
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
       [:h1.h3.mx-2 "create device watch group"]
       [:form.form-control
        [util/render-errors-as-alerts (:errors state-info-system)]
        [util/render-input "name" state-info-name]
        [util/render-textarea "memo" state-info-memo]
        [util/render-select "owner user id" state-info-id-owner-user (model.user/build-select-options-from-list-and-total user-list-and-total)]
        [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
