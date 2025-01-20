(ns front.view.users.password-reset
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.page404 :as page404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-user (get params "id_user")
        hash-password-reset (get params "hash_password_reset")
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-id-user (util/build-state-info :id_user #(react/useState))
        state-info-password (util/build-state-info :password #(react/useState))
        state-info-password-again (util/build-state-info :password_again #(react/useState))
        state-info-show-password (util/build-state-info :show-password #(react/useState))
        [waiting-response set-waiting-response] (react/useState)
        [message set-message] (react/useState)
        on-receive-create-response
        (fn [data errors]
          (set-waiting-response false)
          (if-not (empty? errors)
            ((:set-errors state-info-system) errors)
            (if-let [errors-str (:errors data)]
              (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                (doseq [state [state-info-password state-info-system]]
                  (let [key (:key state)
                        errors-for-key (get errors key)]
                    ((:set-errors state) errors-for-key))))
              (set-message (:message data)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (if-not (= (:draft state-info-password) (:draft state-info-password-again))
            ((:set-errors state-info-password-again) ["unmatch"])
            (do
              ((:set-errors state-info-password-again) nil)
              (set-waiting-response true)
              (model.user/password-reset-by-id-and-hash
               {:id id-user
                :hash hash-password-reset
                :password (:draft state-info-password)
                :on-receive on-receive-create-response}))))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user/fetch-by-id-and-password-reset-hash
        {:id id-user
         :hash hash-password-reset
         :on-receive (fn [user errors]
                       (util/set-default-and-draft state-info-id-user (:id user))
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      [:<>
       (cond
         (not (nil? message))
         [:div
          [:div message]
          [:div
           [:> router/Link {:to route/login} "login"]]]
         (nil? (:default state-info-id-user))
         [:f> page404/core]
         :else
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           (let [show-password (:draft state-info-show-password)
                 type-for-password (if (= show-password "true") :text :password)]
             [:<>
              [util/render-input "password" state-info-password {:type type-for-password}]
              [util/render-input "password again" state-info-password-again {:type type-for-password}]])
           [:div [util/render-checkbox "show password" state-info-show-password]]
           [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply :class (when waiting-response "disabled")} "reset password"]]])]})))

(defn core []
  (page))
