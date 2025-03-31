(ns front.view.profile.password-edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.user :as model.user]))

(defn- page []
  (let [user (router/useRouteLoaderData util/key-user-loggedin)
        navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-show-password (util/build-state-info :show-password #(react/useState))
        state-info-password (util/build-state-info :password #(react/useState))
        state-info-password-new (util/build-state-info :password_new #(react/useState))
        state-info-password-new-again (util/build-state-info :password_new_again #(react/useState))
        [waiting-response set-waiting-response] (react/useState)
        on-receive-password-reset
        (fn [data errors]
          (set-waiting-response false)
          (when-not (empty? errors) ((:set-errors state-info-system) errors))
          (if-let [errors-str (:errors data)]
            (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
              (doseq [state [state-info-system state-info-password state-info-password-new]]
                (let [key (:key state)
                      errors-for-key (get errors key)]
                  ((:set-errors state) errors-for-key))))
            (when-not (empty? (:message data)) (navigate route/profile))))
        on-click-apply (fn []
                         (let [errors-for-password-new-again
                               (if-not (= (:draft state-info-password-new) (:draft state-info-password-new-again))
                                 ["password unmatch"] nil)]
                           ((:set-errors state-info-password-new-again) errors-for-password-new-again)
                           (when (empty? errors-for-password-new-again)
                             (set-waiting-response true)
                             (model.user/reset-password-mine
                              {:password (:draft state-info-password)
                               :password-new (:draft state-info-password-new)
                               :on-receive on-receive-password-reset}))))]
    [:<>
     [:f> breadcrumb/core [{:label (util.label/profile) :path route/profile}
                           {:label util.label/password-edit}]]
     (if (empty? user)
       [:div "no data"]
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         (let [show-password (:draft state-info-show-password)
               type-for-password (if (= show-password "true") :text :password)]
           [:<>
            [util/render-input "password" state-info-password {:type type-for-password}]
            [util/render-input "new password" state-info-password-new {:type type-for-password}]
            [util/render-input "new password again" state-info-password-new-again {:type type-for-password}]])
         [:div [util/render-checkbox "show password" state-info-show-password]]
         [:a.btn.btn-primary.mt-1 {:on-click on-click-apply
                                          :class (when waiting-response "disabled")}
          (util.label/update)]]])]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
