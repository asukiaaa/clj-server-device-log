(ns front.view.util.links
  (:require [front.route :as route]
            [front.model.user :as model.user]
            [front.view.util.label :as util.label]))

(defn build-list-menu-links-for-user [user]
  (->> [(when (model.user/admin? user) [util.label/users route/users])
        [util.label/user-teams route/user-teams]
        [util.label/watch-scopes route/watch-scopes]
        [util.label/device-types route/device-types]
        [util.label/devices route/devices]
        [util.label/profile route/profile]]
       (remove nil?)))
