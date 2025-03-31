(ns front.view.users.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-email (util/build-state-info :email #(react/useState))
        state-info-password (util/build-state-info :password #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive (fn [data errors]
                     (if (seq errors)
                       ((:set-draft state-info-system) errors)
                       (if-let [errors-str (:errors data)]
                         (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                           (doseq [state [state-info-name state-info-email state-info-password state-info-permission]]
                             (let [key (:key state)
                                   errors-for-key (key errors)]
                               ((:set-errors state) errors-for-key))))
                         (when-let [id-user (-> data :user :id)]
                           (navigate (route/user-show id-user))))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (model.user/create
                          {:name (:draft state-info-name)
                           :email (:draft state-info-email)
                           :password (:draft state-info-password)
                           :permission (:draft state-info-permission)
                           :on-receive on-receive}))]
    [:div
     [:f> breadcrumb/core [{:label (util.label/users) :path route/users} {:label (util.label/create)}]]
     [:form.form-control
      [util/render-errors-as-alerts (:errors state-info-system)]
      [util/render-input "name" state-info-name]
      [util/render-input "email" state-info-email]
      [util/render-input "password (10 chars or more)" state-info-password {:type :password}]
      [util/render-textarea "permission" state-info-permission]
      [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/create)]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
