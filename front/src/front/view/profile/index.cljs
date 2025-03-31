(ns front.view.profile.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn- page []
  (let [user (router/useRouteLoaderData util/key-user-loggedin)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        is-admin (util/detect-is-admin-loggedin)]
    [:<>
     [:f> breadcrumb/core [{:label (util.label/profile)}]]
     [util/area-content
      [:> router/Link {:to route/profile-edit} (util.label/edit)]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? user)
         [:div (util.label/no-data)]
         [:div
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [[label value]
                  (->> [[util.label/id (:id user)]
                        [(util.label/name) (:name user)]
                        [util.label/email (:email user)]
                        (when is-admin [util.label/permission (:permission user)])
                        [util.label/password [:> router/Link {:to route/profile-password-edit} util.label/password-edit]]
                        [util.label/created-at (:created_at user)]
                        [util.label/updated-at (:updated_at user)]]
                       (remove nil?))]
              [:tr {:key label} [:td label] [:td value]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
