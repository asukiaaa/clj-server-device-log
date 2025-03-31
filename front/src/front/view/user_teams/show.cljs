(ns front.view.user-teams.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.user-teams.util :as v.team.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.user-team :as model.user-team]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id (get params "user_team_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        is-admin (util/detect-is-admin-loggedin)]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.user-team/fetch-by-id
        {:id id
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/user-teams) :path route/user-teams}
       {:label (or (:name item) (util.label/no-data))}]]
     (util/render-list-in-area-content-line
      (v.team.util/build-related-links item {:id-item id}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            [:tr [:td util.label/id] [:td (:id item)]]
            [:tr [:td (util.label/name)] [:td (:name item)]]
            [:tr [:td util.label/owner-user] [:td [:> router/Link {:to (route/user-show (:owner_user_id item))}(str (:owner_user_id item))]]]
            (when is-admin
              [:tr
               [:td (util.label/action)]
               [:td
                [:f> util/btn-confirm-delete
                 {:message-confirm (model.user-team/build-confirmation-message-for-deleting item)
                  :action-delete #(model.user-team/delete {:id (:id item)
                                                           :on-receive (fn [] (navigate route/user-teams))})}]]])
            (for [key [:created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td (get item key)]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
