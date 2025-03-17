(ns front.view.util.device-log.page
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.view.util.device-log.graph :as util.graph]
            [front.view.util.device-log.list :as util.list]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.component.pagination :as pagination]
            [front.view.util :as util :refer [build-state-info render-checkbox render-input render-textarea]]
            [front.view.util.label :as util.label]))

(def map-page-default
  {:str-renderer "[{\"key\":\"id\"},{\"key\":\"data\"},{\"key\":\"created_at\"}]"
   :str-where "[]"
   :str-order "[{\"key\":\"created_at\",\"dir\":\"desc\"}]"
   :show-graph "false"
   :show-table "true"
   :limit "100"})

(defn parse-json [text]
  (let [parse-default-value #(.parse js/JSON text)
        parsed-js-value (try (parse-default-value) (catch js/Error _ nil))
        is-invalid-json (nil? parsed-js-value)
        parsed-value (when (not is-invalid-json) (js->clj parsed-js-value))
        error-message (when is-invalid-json (try (parse-default-value) (catch js/Error e e)))]
    [parsed-value error-message]))

(defn get-param-str [key query-params map-default]
  (or (get query-params key) (get map-default key)))

(defn set-all-val [val info]
  ((:set-draft info) val)
  ((:set-default info) val))

(defn get-default-as-bool [info]
  (= "true" (:default info)))

(defn core [fetch-list-and-total & {:keys [map-default on-receive]}]
  (let [map-default (merge map-page-default map-default)
        location (router/useLocation)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        fetching (:fetching info-wrapper-fetching)
        [logs set-logs] (react/useState)
        [logs-key-fetched set-logs-key-fetched] (react/useState)
        [total set-total] (react/useState)
        info-limit (build-state-info :limit #(react/useState))
        info-str-renderer (build-state-info :str-renderer #(react/useState))
        info-str-where (build-state-info :str-where #(react/useState))
        info-str-order (build-state-info :str-order #(react/useState))
        info-show-graph (build-state-info :show-graph #(react/useState false))
        info-show-table (build-state-info :show-table #(react/useState true))
        total-page (pagination/calc-total-page (:default info-limit) total)
        query-params (util/read-query-params)
        page-current (or (pagination/key-page query-params) 0)
        arr-info [info-limit info-str-renderer info-str-order info-str-where info-show-graph info-show-table]
        [show-config set-show-config] (react/useState false)
        [config-renderer parse-error-config-renderer] (parse-json (:default info-str-renderer))
        [_ parse-error-where] (parse-json (:default info-str-where))
        [_ parse-error-order] (parse-json (:default info-str-order))
        load-query-params #(let [query-params (util/read-query-params)]
                             (doseq [info arr-info]
                               (-> (get-param-str (:key info) query-params map-default)
                                   (set-all-val info))))
        fetch-device-logs (fn [str-where str-order limit]
                            (let [logs-key (str str-where str-order limit)]
                              (wrapper.fetching/start info-wrapper-fetching)
                              (fetch-list-and-total
                               {:str-order str-order
                                :str-where str-where
                                :limit limit
                                :page page-current
                                :on-receive
                                (fn [data errors]
                                  #_(println data errors)
                                  (when-not (nil? on-receive) (on-receive data))
                                  (set-logs (:list data))
                                  (set-total (:total data))
                                  (set-logs-key-fetched logs-key)
                                  (wrapper.fetching/finished info-wrapper-fetching errors))})))
        on-click-apply (fn []
                         (->> (for [info arr-info] [(:key info) (:draft info)])
                              (into (sorted-map))
                              (#(assoc % pagination/key-page 0))
                              util/push-query-params)
                         (load-query-params))
        show-pagination (and (not show-config) (not (= total (count logs))))]
    (react/useEffect
     (fn []
       (load-query-params)
       (fn []))
     #js [location])
    (let [str-where (:default info-str-where)
          str-order (:default info-str-order)
          limit (:default info-limit)]
      (react/useEffect
       (fn []
         (when-not (or (empty? str-where) (empty? str-order))
           (fetch-device-logs str-where str-order limit))
         (fn []))
       #js [str-where str-order limit page-current]))
    [:div
     [:a.btn.btn-outline-primary.btn-sm.m-2 {:on-click #(set-show-config (not show-config))}
      (if show-config "hide config" "show config")]
     [:form.form-control {:style {:display (if show-config "block" "none")}}
      [render-textarea "renderer" info-str-renderer parse-error-config-renderer]
      [render-textarea "where" info-str-where parse-error-where]
      [render-textarea "order" info-str-order parse-error-order]
      [:div
       [render-input "limit" info-limit {:type "number"}]]
      [:div
       [render-checkbox "show graph" info-show-graph]
       [render-checkbox "show table" info-show-table]]
      [:a.btn.btn-outline-primary.btn-sm {:on-click on-click-apply :class (when fetching "disabled")} "apply"]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [util/area-content
         (when-not (nil? total)
           (util.label/display-page-limit-total page-current (:default info-limit) total))]
        (when show-pagination
          [util/area-content
           [:f> pagination/core {:total-page total-page}]])
        (when (get-default-as-bool info-show-graph)
          [:f> util.graph/core logs-key-fetched logs config-renderer])
        (when (get-default-as-bool info-show-table)
          [:f> util.list/core logs config-renderer])
        (when show-pagination
          [util/area-content
           [:f> pagination/core {:total-page total-page}]])]})]))
