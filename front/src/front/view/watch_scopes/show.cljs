(ns front.view.watch-scopes.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.util.timezone :as util.timezone]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn render-term [term]
  (let [device (:device term)]
    [:div
     [:> router/Link {:to (route/device-show (:id device))} (util.label/device-item device)]
     " "
     (let [datetime-from (:datetime_from term)
           datetime-until (:datetime_until term)]
       (if (and (nil? datetime-from) (nil? datetime-until))
         util.label/no-term
         [:<>
          (when datetime-from
            (util.label/datetime-from-item
             (util.timezone/build-datetime-str-in-timezone
              datetime-from
              {:datetime-format util.timezone/date-fns-format-datetime-until-minutes-with-timezone})))
          (when datetime-until
            (util.label/datetime-until-item
             (util.timezone/build-datetime-str-in-timezone
              datetime-until
              {:datetime-format util.timezone/date-fns-format-datetime-until-minutes-with-timezone})))]))]))

(defn render-terms [terms]
  (for [term terms]
    [:<> {:key (:id term)}
     (render-term term)]))

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
                  (render-terms (key item))
                  :else
                  (get item key))]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
