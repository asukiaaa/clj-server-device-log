(ns front.view.users.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common-layout :as common-layout]
            [front.model.user :as model.user]))

(defn core []
  (let [params (js->clj (router/useParams))
        id-user (get params "idUser")
        [user set-user] (react/useState)
        info-common-layout (common-layout/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (common-layout/fetch-start info-common-layout)
       (model.user/fetch-by-id {:id id-user
                                :on-receive (fn [user errors]
                                              (set-user user)
                                              (common-layout/fetch-finished info-common-layout errors))})
       (fn []))
     #js [])
    (common-layout/wrapper
     info-common-layout
     (if (empty? user)
       [:div "no data"]
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
             [:td (get user key)]])]]]))))
