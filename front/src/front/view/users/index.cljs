(ns front.view.users.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.user :as model.user]))

(defn render-user [user on-delete]
  [:tr
   [:td (:id user)]
   [:td (:email user)]
   [:td (:name user)]
   [:td (:created_at user)]
   [:td (:updated_at user)]
   [:td
    [:> router/Link {:to (route/user-show (:id user))} "show"]
    " "
    [:> router/Link {:to (route/user-edit (:id user))} "edit"]
    " "
    [:a {:on-click
         (fn [e]
           (.preventDefault e)
           (when (js/confirm (str "delete user id:" (:id user) " name:" (:name user)))
             (model.user/delete {:id (:id user) :on-receive on-delete})))
         :href ""}
     "delete"]]])

(defn-  page []
  (let [[user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        users (:list user-list-and-total)
        total (:total user-list-and-total)
        load-list (fn []
                    (wrapper.fetching/start info-wrapper-fetching)
                    (model.user/fetch-list-and-total
                     {:on-receive (fn [result errors]
                                    (set-user-list-and-total result)
                                    (wrapper.fetching/finished info-wrapper-fetching errors))}))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [])
    [:<>
     [:> router/Link {:to route/user-create} "new"]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:<>
        [:div "total " total]
        [:table.table.table-sm
         [:thead
          [:tr
           [:th "id"]
           [:th "email"]
           [:th "name"]
           [:th "created_at"]
           [:th "updated_at"]
           [:th "actions"]]]
         [:tbody
          (for [user users]
            [:<> {:key (:id user)}
             [:f> render-user user load-list]])]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission :admin
    :page page}))
