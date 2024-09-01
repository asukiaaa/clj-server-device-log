(ns front.view.common.wrapper.show404
  (:require [front.view.page404 :as page404]
            [front.model.user :as model.user]
            [front.view.util :as util]))

(def permission-admin :admin)
(def permission-login :login) ; TODO redirect to login page

(defn show-page? [{:keys [user permission]}]
  (cond (= permission permission-admin) (model.user/admin? user)
        (= permission permission-login) (not-empty user)
        :else false))

(defn wrapper [{:keys [permission show-page page]}]
  (let [user (util/get-user-loggedin)
        show-page (if-not (nil? show-page) show-page (show-page? {:user user :permission permission}))]
    (if show-page (page) (page404/core))))
