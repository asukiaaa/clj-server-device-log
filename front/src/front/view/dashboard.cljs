(ns front.view.dashboard
  (:require ["react" :as react]
            ["react-bootstrap" :as bs]
            ["react-router-dom" :as router]
            [front.model.device-file :as model.device-file]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.links :as util.links]
            [front.view.util.device-file.page :as file.page]
            [front.view.util :as util]
            [front.route :as route]
            [front.view.util.label :as util.label]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.device :as model.device]))

(defn page []
  (let [user-loggedin (util/get-user-loggedin)
        key-order-by-watch-scope-name :order_by_watch_scope_name
        [js-query-params set-search-params] (router/useSearchParams)
        query-params (util/search-params->map js-query-params)
        order-by-watch-scope-name (= "true" (:order_by_watch_scope_name query-params))
        state-info-str-search (util/build-state-info :str_search react/useState {:default (:str_search query-params)})
        [received set-received] (react/useState false)
        on-receive #(set-received true)
        location (router/useLocation)
        fetch-list-and-total
        (fn [params]
          (model.device-file/fetch-list-and-total-latest-each-device
           (assoc params
                  :str_search (:str_search query-params)
                  :allow_duplicate_for_watch_scope order-by-watch-scope-name
                  :order (when order-by-watch-scope-name
                           (->> [{:key (str model.watch-scope/name-table ".name") :dir "IS NULL ASC"}
                                 {:key (str model.watch-scope/name-table ".name") :dir "asc"}
                                 {:key (str model.device/name-table ".name") :dir "desc"}
                                 {:key (str model.device-file/name-table ".recorded_at") :dir "desc"}]
                                clj->js
                                (.stringify js/JSON))))))
        on-click-search
        (fn [e]
          (.preventDefault e)
          (when received
            (set-received false)
            (-> query-params
                (assoc :str_search (:draft state-info-str-search))
                clj->js set-search-params)))]
    (react/useEffect
     (fn []
       ((:set-draft state-info-str-search) (:str_search query-params))
       (fn []))
     #js [location])
    [:<>
     [:> bs/Container {:fluid true}
      [:> bs/Row
       [:> bs/Col {:sm 2 :class :px-0}
        [:f> breadcrumb/core []]
        [:div.list-group
         (for [[name url] (util.links/build-list-menu-links-for-user user-loggedin)]
           [:<> {:key name}
            [:> router/Link {:to url :class "list-group-item list-group-item-action"} name]])]]
       [:> bs/Col {:class :px-0}
        [:div.px-2
         [util/render-link-or-text (util.label/order-by-device)
          {:to (str route/front "?" (util/clj-search-params->str (dissoc query-params key-order-by-watch-scope-name)))}
          {:show-text (not order-by-watch-scope-name)}]
         " "
         [util/render-link-or-text (util.label/order-by-watch-scope)
          {:to (str route/front "?" (util/clj-search-params->str (assoc query-params key-order-by-watch-scope-name true))) :disabled order-by-watch-scope-name}
          {:show-text order-by-watch-scope-name}]]
        [:form.form
         [util/render-input nil state-info-str-search
          {:str-class-wrapper "input-group px-2"
           :after-input
           [:button.btn.btn-outline-primary {:on-click on-click-search :disabled (not received)} (util.label/search)]}]]
        [:div {:key location}
         [:f> file.page/core fetch-list-and-total {:on-receive on-receive}]]]]]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
