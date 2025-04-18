(ns front.view.user-teams.members.util
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.view.util :as util]))

(defn build-related-links [item & [{:keys [id-item id-team-member]}]]
  (let [location (router/useLocation)
        path-current (.-pathname location)
        id-item (or id-item (:user_team_id item))
        id-team-member (or id-team-member (:id item))]
    (->> [(util/build-link-or-text (util.label/show) (route/user-team-member-show id-item id-team-member) path-current)
          (util/build-link-or-text (util.label/edit) (route/user-team-member-edit id-item id-team-member) path-current)]
         (remove nil?))))
