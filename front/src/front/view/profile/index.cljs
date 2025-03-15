(ns front.view.profile.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn- page []
  (let [user (router/useRouteLoaderData util/key-user-loggedin)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    [:<>
     [:f> breadcrumb/core [{:label util.label/profile}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? user)
         [:div util.label/no-data]
         [:div
          " "
          #_[:f> util/btn-confirm-delete
             {:message-confirm (model.user/build-confirmation-message-for-deleting user)
              :action-delete #(model.user/delete {:id (:id user)
                                                  :on-receive (fn [] (navigate route/users))})}]
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key [:id :email :name :permission :password :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td (cond
                      (= key :password)
                      [:> router/Link {:to route/profile-password-edit} util.label/password-edit]
                      :else
                      (get user key))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
