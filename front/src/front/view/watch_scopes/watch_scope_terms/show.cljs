(ns front.view.watch-scopes.watch-scope-terms.show
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.watch-scope-term :as model.watch-scope-term]))

(defn- page []
  (let [params (js->clj (router/useParams))
        navigate (router/useNavigate)
        id-watch-scope (get params "watch_scope_id")
        id-watch-scope-term (get params "watch_scope_term_id")
        [item set-item] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.watch-scope-term/fetch-by-id-for-watch-scope
        {:id id-watch-scope-term
         :id-watch-scope id-watch-scope
         :on-receive (fn [item errors]
                       (set-item item)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:> router/Link {:to (route/watch-scope-watch-scope-terms id-watch-scope)} "index"]
         " "
         [:> router/Link {:to (route/watch-scope-watch-scope-term-edit id-watch-scope id-watch-scope-term)} "edit"]
         " "
         [:f> util/btn-confirm-delete
          {:message-confirm (model.watch-scope-term/build-confirmation-message-for-deleting item)
           :action-delete #(model.watch-scope-term/delete
                            {:id (:id item)
                             :on-receive (fn [] (navigate (route/watch-scope-watch-scope-terms id-watch-scope)))})}]
         [:table.table.table-sm
          [:thead
           [:tr
            [:th "key"]
            [:th "value"]]]
          [:tbody
           (for [key [:id :display_name :device :watch_scope_id :created_at :updated_at]]
             [:tr {:key key}
              [:td (cond
                     (= :device key) "device id name"
                     :else key)]
              [:td (cond
                     (= :device key) (str (:device_id item) " " (:device_name item))
                     :else (get item key))]])]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
