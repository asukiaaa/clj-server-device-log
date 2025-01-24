(ns front.view.users.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.user :as model.user]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-user (get params "id_user")
        [user set-user] (react/useState)
        [hash-to-reset-password set-hash-to-reset-password] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        [waiting-response set-waiting-response] (react/useState)
        on-receive-hash-reset-password
        (fn [data errors]
          (when errors ((:set-draft state-info-system) errors))
          (when-let [hash (:hash data)]
            (set-hash-to-reset-password hash)))
        create-link-to-reset-password
        (fn []
          (set-waiting-response true)
          (model.user/create-hash-to-reset-password {:id-user id-user
                                                     :on-receive on-receive-hash-reset-password}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user/fetch-by-id {:id id-user
                                :on-receive (fn [user errors]
                                              (set-user user)
                                              (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/users :path route/users}
                           {:label (or (:name user) util.label/no-data)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? user)
         [:div util.label/no-data]
         [:div
          (if (empty? hash-to-reset-password)
            [:div
             [:div.btn.btn-outline-primary
              {:on-click create-link-to-reset-password
               :class (when waiting-response "disable")}
              "crete link to reset password"]]
            (let [url-object (util/build-current-url-object)
                  url (str (.-origin url-object) (route/user-password-reset id-user hash-to-reset-password))]
              [:div
               [:div.btn.btn-outline-primary {:on-click #(util/copy-to-clipboard url)}
                "copy url to reset password"]
               [:span url]]))
          [:> router/Link {:to (route/user-edit id-user)} util.label/edit]
          " "
          [:f> util/btn-confirm-delete
           {:message-confirm (model.user/build-confirmation-message-for-deleting user)
            :action-delete #(model.user/delete {:id (:id user)
                                                :on-receive (fn [] (navigate route/users))})}]
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key [:id :email :name :permission :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td (get user key)]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
