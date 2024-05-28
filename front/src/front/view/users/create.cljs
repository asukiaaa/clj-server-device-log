(ns front.view.users.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util :as util]))

(defn core []
  (let [navigate (router/useNavigate)
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-email (util/build-state-info :email #(react/useState))
        state-info-password (util/build-state-info :password #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive (fn [data]
                     (if-let [errors-str (:errors data)]
                       (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                         (doseq [state [state-info-name state-info-email state-info-password state-info-permission]]
                           (let [key (:key state)
                                 errors-for-key (get errors key)]
                             ((:set-errors state) errors-for-key))))
                       (when-let [id-user (-> data :user :id)]
                         (navigate (route/user-show id-user)))))
        on-click-apply (fn [] (model.user/create
                               {:name (:draft state-info-name)
                                :email (:draft state-info-email)
                                :password (:draft state-info-password)
                                :permission (:draft state-info-permission)
                                :on-receive on-receive}))]
    [:div
     [:form.form-control
      [util/render-input "name" state-info-name]
      [util/render-input "email" state-info-email]
      [util/render-input "password (10 chars or more)" state-info-password {:type :password}]
      [util/render-textarea "permission" state-info-permission]
      [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]]))
