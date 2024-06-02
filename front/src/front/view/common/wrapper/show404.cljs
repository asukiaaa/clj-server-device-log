(ns front.view.common.wrapper.show404
  (:require [front.view.page404 :as page404]
            [front.model.user :as model.user]
            [front.view.util :as util]))

(defn show-page? [{:keys [user permission]}]
  (cond (= permission :admin) (model.user/admin? user)
        :else false))

(defn wrapper [{:keys [user permission show-page page]}]
  (let [user (util/get-user-loggedin)
        show-page (if-not (nil? show-page) show-page (show-page? {:user user :permission permission}))]
    (if show-page (page) (page404/core))))
