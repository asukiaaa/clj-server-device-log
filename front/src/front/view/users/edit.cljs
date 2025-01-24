(ns front.view.users.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-user (get params "id_user")
        navigate (router/useNavigate)
        [user set-user] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-email (util/build-state-info :email #(react/useState))
        state-info-permission (util/build-state-info :permission #(react/useState))
        on-receive-user
        (fn [user]
          (set-user user)
          (util/set-default-and-draft state-info-email (:email user))
          (util/set-default-and-draft state-info-name (:name user))
          (util/set-default-and-draft state-info-permission (:permission user)))
        on-receive-create-response
        (fn [data errors]
          (if (seq errors)
            ((:set-draft state-info-system) errors)
            (if-let [errors-str (:errors data)]
              (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                (doseq [state [state-info-name state-info-email state-info-permission state-info-system]]
                  (let [key (:key state)
                        errors-for-key (get errors key)]
                    ((:set-errors state) errors-for-key))))
              (when-let [id-user (-> data :user :id)]
                (navigate (route/user-show id-user))))))
        on-click-apply (fn [e]
                         (.preventDefault e)
                         (model.user/update
                          {:id id-user
                           :name (:draft state-info-name)
                           :email (:draft state-info-email)
                           :permission (:draft state-info-permission)
                           :on-receive on-receive-create-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user/fetch-by-id {:id id-user
                                :on-receive (fn [user errors]
                                              (on-receive-user user)
                                              (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/users :path route/users}
                           {:label (or (:name user) util.label/no-data) :path (route/user-show id-user)}
                           {:label util.label/edit}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? (:default state-info-email))
         [:div
          "no data"]
         [:div
          [:form.form-control
           [util/render-input "name" state-info-name]
           [util/render-input "email" state-info-email]
           [util/render-textarea "permission" state-info-permission]
           [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
