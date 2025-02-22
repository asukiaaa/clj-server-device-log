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
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id (get params "watch_scope_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
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
      [{:label util.label/watch-scopes :path route/watch-scopes}
       {:label (util.label/watch-scope-item item)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:> router/Link {:to (route/watch-scope-edit id)} util.label/edit]
          " "
          [:f> util/btn-confirm-delete
           {:message-confirm (model.watch-scope/build-confirmation-message-for-deleting item)
            :action-delete #(model.watch-scope/delete {:id (:id item)
                                                       :on-receive (fn [] (navigate route/watch-scopes))})}]
          " "
          [:> router/Link {:to (route/watch-scope-device-logs id)} util.label/logs]
          [:table.table.table-sm
           [:thead
            [:tr
             [:th "key"]
             [:th "value"]]]
           [:tbody
            (for [key [:id :user_team_id :name :terms :created_at :updated_at]]
              [:tr {:key key}
               [:td key]
               [:td
                (cond
                  (= key :terms)
                  (util.watch-scope/render-terms (key item))
                  :else
                  (get item key))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
