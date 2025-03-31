(ns front.view.util.links
  (:require [front.route :as route]
            [front.view.util.label :as util.label]))

(defn build-list-menu-links-for-user [_user]
  (->> [[(util.label/watch-scopes) route/watch-scopes]
        [(util.label/devices) route/devices]
        [(util.label/device-types) route/device-types]
        [(util.label/user-teams) route/user-teams]
        [(util.label/users) route/users]
        [(util.label/profile) route/profile]]
       (remove nil?)))
