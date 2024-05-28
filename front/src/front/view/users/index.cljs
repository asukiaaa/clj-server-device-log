(ns front.view.users.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]))

(defn render-user [user]
  [:tr
   [:td (:id user)]
   [:td (:email user)]
   [:td (:name user)]
   [:td (:created_at user)]
   [:td (:updated_at user)]
   [:td [:> router/Link {:to (route/user-show (:id user))} "show"]]])

(defn core []
  (let [[user-list-and-total set-user-list-and-total] (react/useState)
        users (:list user-list-and-total)
        total (:total user-list-and-total)]
    (react/useEffect
     (fn []
       (println :trigger-fetch-users)
       (model.user/fetch-list-and-total {:on-receive #(set-user-list-and-total %)})
       (fn []))
     #js [])
    [:div
     [:> router/Link {:to route/user-create} "new"]
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
          [:f> render-user user]])]]]))
