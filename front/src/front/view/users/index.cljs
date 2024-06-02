(ns front.view.users.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.model.user :as model.user]))

(defn render-user [user]
  [:tr
   [:td (:id user)]
   [:td (:email user)]
   [:td (:name user)]
   [:td (:created_at user)]
   [:td (:updated_at user)]
   [:td [:> router/Link {:to (route/user-show (:id user))} "show"]]])

(defn-  page []
  (let [[user-list-and-total set-user-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        users (:list user-list-and-total)
        total (:total user-list-and-total)]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user/fetch-list-and-total
        {:on-receive (fn [result errors]
                       (set-user-list-and-total result)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
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
             [:f> render-user user]])]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission :admin
    :page page}))
