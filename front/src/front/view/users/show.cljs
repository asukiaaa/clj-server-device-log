(ns front.view.users.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]))

(defn core []
  (let [params (js->clj (router/useParams))
        id-user (get params "idUser")
        [user set-user] (react/useState)]
    (react/useEffect
     (fn []
       (model.user/fetch-by-id {:id id-user :on-receive set-user})
       (fn []))
     #js [])
    (if (empty? user)
      [:div "fetching"]
      [:div
       "TODO: " [:> router/Link {:to (route/user-edit id-user)} "edit"]
       [:table.table.table-sm
        [:thead
         [:tr
          [:th "key"]
          [:th "value"]]]
        [:tbody
         (for [key [:id :email :name :permission :created_at :updated_at]]
           [:tr {:key key}
            [:td key]
            [:td (get user key)]])]]])))
