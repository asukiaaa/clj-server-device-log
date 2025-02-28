(ns front.view.user-teams.util
  (:require [front.route :as route]
            [front.view.util.label :as util.label]))

(defn build-related-links [id-user-team]
  [[util.label/member (route/user-team-members id-user-team)]])
