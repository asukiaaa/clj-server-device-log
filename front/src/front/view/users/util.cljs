(ns front.view.users.util
  (:require [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item load-list user-loggedin & [{:keys [path-current]}]]
  (let [id-user (:id item)]
    (->> [(util/build-link-or-text util.label/show (route/user-show id-user) path-current)
          (when (model.user/admin? user-loggedin)
            (util/build-link-or-text util.label/edit (route/user-edit id-user) path-current))
          (when (model.user/admin? user-loggedin)
            [:f> util/btn-confirm-delete
             {:message-confirm (model.user/build-confirmation-message-for-deleting item)
              :action-delete #(model.user/delete {:id id-user :on-receive load-list})}])]
         (remove nil?))))
