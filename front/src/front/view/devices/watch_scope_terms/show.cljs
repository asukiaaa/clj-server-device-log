(ns front.view.devices.watch-scope-terms.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.util.device :as m.util.device]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.watch-scope-term :as model.watch-scope-term]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        ids-user-team-editable (util/get-ids-user-team-editable)
        id-device (get params "device_id")
        id-item (get params "watch_scope_term_id")
        [device set-device] (react/useState)
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        editable (or (util/detect-is-admin-loggedin)
                     (m.util.device/detect-editable-for-user-team device ids-user-team-editable))
        on-delete #(navigate (route/device-watch-scope-terms id-device))
        fetch-watch-scope-term
        (fn [errors next]
          (model.watch-scope-term/fetch-by-id
           {:id id-item
            :on-receive (fn [item new-errors]
                          (set-device (:device item))
                          (set-item item)
                          (next (concat errors new-errors)))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (fetch-watch-scope-term
        nil
        (fn [errors]
          (wrapper.fetching/finished info-wrapper-fetching errors))
        #_(fn [errors]
            (fetch-watch-scopes
             errors
             (wrapper.fetching/finished info-wrapper-fetching errors))))
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/devices) :path route/devices}
                           {:label (util.label/device-item device)
                            :path (when item (route/device-show id-device))}
                           {:label (util.label/term-of-watch-scope)
                            :path (route/device-watch-scope-terms id-device)}
                           {:label (util.label/show)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div (util.label/no-data)]
         [:div
          [:table.table.table-sm
           [:thead
            [:tr
             [:th (util.label/element)]
             [:th (util.label/value)]]]
           [:tbody
            [:tr
             [:td (util.label/device)]
             [:td [:> router/Link {:to (route/device-show (:device_id item))} (-> item :device :name)]]]
            [:tr
             [:td (util.label/watch-scope)]
             [:td [:> router/Link {:to (route/watch-scope-show (:watch_scope_id item))} (-> item :watch_scope :name)]]]
            [:tr
             [:td (util.label/term)]
             [:td (util.watch-scope/render-term-from-to item)]]
            (when editable
              [:tr
               [:td (util.label/action)]
               [:td
                [:> router/Link {:to (route/device-watch-scope-term-edit id-device id-item)} (util.label/edit)]
                " "
                [:f> util/btn-confirm-delete
                 {:message-confirm (model.watch-scope-term/build-confirmation-message-for-deleting item)
                  :action-delete #(model.watch-scope-term/delete {:id id-item :on-receive on-delete})}]]])]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
