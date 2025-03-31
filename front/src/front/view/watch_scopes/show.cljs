(ns front.view.watch-scopes.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]
            [front.view.watch-scopes.util :as v.watch-scope.util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id (get params "watch_scope_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        navigate (router/useNavigate)
        on-delete
        (fn [{:keys [errors]}]
          (if (nil? errors)
            (navigate route/watch-scopes)
            (js/alert (str errors))))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.watch-scope/fetch-by-id
        {:id id
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/watch-scopes) :path route/watch-scopes}
       {:label (util.label/watch-scope-item item)}]]
     (util/render-list-in-area-content-line
      (v.watch-scope.util/build-related-links item))
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
            (for [key [:name :user_team :terms :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td
                (cond
                  (= key :terms)
                  (util.watch-scope/render-terms (key item))
                  (= key :user_team)
                  (if-let [team (:user_team item)]
                    [:> router/Link {:to (route/user-team-show (:id team))} (util.label/user-team-item team)]
                    (util.label/no-data))
                  :else
                  (get item key))]])
            [:tr
             [:td (util.label/action)]
             [:td
              [:f> util/btn-confirm-delete
               {:message-confirm (model.watch-scope/build-confirmation-message-for-deleting item)
                :action-delete #(model.watch-scope/delete {:id id :on-receive on-delete})}]]]]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
