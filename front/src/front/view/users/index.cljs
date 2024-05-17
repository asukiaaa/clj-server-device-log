(ns front.view.users.index
  (:require ["react" :as react]
            [front.model.user :as model.user]))

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
     [:div "total " total]
     (for [user users]
       (let [id (:id user)]
         [:div {:key id} (str user)]))]))
