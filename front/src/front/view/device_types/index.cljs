(ns front.view.device-types.index
  (:require ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.device-type :as model.device-type]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]))

(defn render-device-type [device-type]
  (let [user-team (model.device-type/key-manager-user-team device-type)]
    [:tr
     [:td (:name device-type)]
     [:td [:> router/Link {:to (route/user-team-show (:id user-team))} (util.label/user-team-item user-team)]]
     [:td
      (util/render-list-inline
       (v.device-type.util/build-related-links device-type))]]))

(defn- page []
  (let [labels-header [(util.label/name) (util.label/user-team) (util.label/action)]
        is-admin (util/detect-is-admin-loggedin)]
    [:<>
     [:f> breadcrumb/core [{:label (util.label/device-types)}]]
     [util/area-content
      (when is-admin
        [:> router/Link {:to route/device-type-create} (util.label/create)])]
     [:f> util.table/core model.device-type/fetch-list-and-total labels-header render-device-type]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
