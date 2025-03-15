(ns front.view.users.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn- build-link [label path path-current]
  (if (= path path-current)
    [:span label]
    [:> router/Link {:to path} label]))

(defn build-related-links [item on-delete user-loggedin & [{:keys [path-current]}]]
  (let [id-user (:id item)]
    (->> [(build-link util.label/show (route/user-show id-user) path-current)
          (when (model.user/admin? user-loggedin)
            (build-link util.label/edit (route/user-edit id-user) path-current))
          (when (model.user/admin? user-loggedin)
            [:f> util/btn-confirm-delete
             {:message-confirm (model.user/build-confirmation-message-for-deleting item)
              :action-delete #(model.user/delete {:id id-user :on-receive on-delete})}])]
         (remove nil?))))
