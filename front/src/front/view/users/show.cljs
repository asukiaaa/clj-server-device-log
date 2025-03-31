(ns front.view.users.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.users.util :as v.user.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.user :as model.user]))

(defn- page []
  (let [params (js->clj (router/useParams))
        location (router/useLocation)
        path-current (.-pathname location)
        navigate (router/useNavigate)
        user-loggedin (util/get-user-loggedin)
        is-admin (model.user/admin? user-loggedin)
        id-user (get params "user_id")
        [user set-user] (react/useState)
        [hash-to-reset-password set-hash-to-reset-password] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        [waiting-response set-waiting-response] (react/useState)
        on-delete #(model.user/delete {:id (:id user)
                                       :on-receive (fn [] (navigate route/users))})
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
     [:f> breadcrumb/core [{:label (util.label/users) :path route/users}
                           {:label (util.label/user-item user)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? user)
         [:div (util.label/no-data)]
         [:<>
          (when is-admin
            [util/area-content
             (util/render-list
              (v.user.util/build-related-links user on-delete user-loggedin {:path-current path-current})
              (fn [link] [:<> link " "]))])
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key (->> [:name :email (when is-admin :permission) (when is-admin :password) :created_at :updated_at]
                           (remove nil?))]
              (cond
                (= key :password)
                [:tr {:key key}
                 [:td key]
                 [:td
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
                       [:div url]]))]]
                :else
                [:tr {:key key}
                 [:td key]
                 [:td (get user key)]]))]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
