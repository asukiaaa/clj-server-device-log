(ns front.view.device-types.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.device-types.util :as v.device-type.util]
            [front.view.util :as util]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.model.device-type :as model.device-type]
            [front.model.util.device-type :as model.util.device-type]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id (get params "device_type_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        navigate (router/useNavigate)
        on-delete (fn [] (navigate route/device-types))
        is-admin (util/detect-is-admin-loggedin)]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.device-type/fetch-by-id
        {:id id
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/device-types) :path route/device-types}
                           {:label (util.label/device-type-item item)}]]
     (util/render-list-in-area-content-line
      (v.device-type.util/build-related-links item {:id-item id}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:<>
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key model.util.device-type/keys-for-table]
              [:tr {:key key}
               (cond
                 (= key :manager_user_team_id)
                 (let [user-team (model.util.device-type/key-manager-user-team item)]
                   [:<>
                    [:td util.label/manager-user-team]
                    [:td [:> router/Link {:to (route/user-team-show (:id user-team))}
                          (util.label/user-team-item user-team)]]])
                 :else
                 [:<>
                  [:td key]
                  [:td (get item key)]])])
            (when is-admin
              [:tr
               [:td (util.label/action)]
               [:td
                [:f> util/btn-confirm-delete
                 {:message-confirm (model.device-type/build-confirmation-message-for-deleting item)
                  :action-delete #(model.device-type/delete {:id id :on-receive on-delete})}]]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
