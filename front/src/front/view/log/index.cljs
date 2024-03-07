(ns front.view.log.index
  (:require ["react" :as react]
            [front.view.log.graph :as log.graph]
            [front.view.log.list :as log.list]
            [front.model.raw-device-log :as model.log]))

(defn push-params [query-params]
  ;; (println "push-url")
  (let [url js/window.location.href
        url-object (new js/URL url)
        url-search-params (.-searchParams url-object)]
    (doseq [[k v] query-params]
      #_(println "in for" k v)
      (.set url-search-params (name k) v))
    ;; (println "url is" url)
    ;; (println "url-object" url-object)
    ;; (println "url-search-params" url-search-params)
    (js/history.pushState nil nil (str "?" url-search-params))))

(defn read-params []
  ;; (println "read-params")
  (let [url js/window.location.href
        url-object (new js/URL url)
        url-search-params (.-searchParams url-object)]
    ;; (println "url is" url)
    ;; (println "url-object" url-object)
    ;; (println "url-search-params" url-search-params)
    ;; (println "cljs params" (js->clj url-search-params))
    (into {} (for [key (.keys url-search-params)]
               [(keyword key) (.get url-search-params key)]))))

(def defaults
  {:str-renderer "[{\"key\": [\"data\", \"camera_id\"], \"badge\": [{\"text\": \"not wakeup\", \"when\": {\"key\": \"created_at\", \"action\": \"not-in-hours-24\"}}]}, {\"label\": \"battery\", \"key\": [\"data\", \"readonly_state\", \"volt_battery\"]}, {\"label\": \"panel\", \"key\": [\"data\", \"readonly_state\", \"volt_panel\"]}]"
   :str-where "[{\"key\": \"created_at\", \"action\": \"in-hours-24\"}]"
   :str-order "[{\"key\": [\"data\", \"camera_id\"], \"dir\": \"desc\"},{\"key\":\"created_at\",\"dir\":\"desc\"}]"
   :show-graph false
   :show-table true})

(defn parse-json [text]
  (let [parse-default-value #(.parse js/JSON text)
        parsed-js-value (try (parse-default-value) (catch js/Error _ nil))
        is-invalid-json (nil? parsed-js-value)
        parsed-value (when (not is-invalid-json) (js->clj parsed-js-value))
        error-message (when is-invalid-json (try (parse-default-value) (catch js/Error e e)))]
    [parsed-value error-message]))

(defn render-textarea-json [label str-json set-draft error-message]
  [:div
   [:div label]
   [:div (str error-message)]
   [:textarea.form-control.mb-1
    {:type :text :default-value str-json :key str-json
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}]])

(defn render-checkbox [label value-draft set-value-draft]
  [:span
   [:input {:id label
            :type "checkbox"
            :checked value-draft
            :on-change (fn [] (set-value-draft (not value-draft)))}]
   [:label.p-2 {:for label} label]])

(defn get-param-str [key query-params]
  (or (get query-params key) (get defaults key)))

(defn get-param-bool [key query-params]
  (= "true" (get-param-str key query-params)))

(defn core []
  (let [query-params (read-params)
        [logs set-logs] (react/useState)
        [logs-key set-logs-key] (react/useState)
        [total set-total] (react/useState)
        [limit set-limit] (react/useState 100)
        [str-renderer set-str-renderer] (react/useState)
        [str-draft-renderer set-str-draft-renderer] (react/useState)
        [str-where set-str-where] (react/useState)
        [str-draft-where set-str-draft-where] (react/useState)
        [str-order set-str-order] (react/useState)
        [str-draft-order set-str-draft-order] (react/useState)
        [show-graph set-show-graph] (react/useState false)
        [show-config set-show-config] (react/useState false)
        [draft-show-graph set-draft-show-graph] (react/useState show-graph)
        [config-renderer parse-error-config-renderer] (parse-json str-renderer)
        [_ parse-error-where] (parse-json str-where)
        [_ parse-error-order] (parse-json str-order)
        load-query-params
        (fn []
          (let [query-params (read-params)
                str-renderer (get-param-str :str-renderer query-params)
                str-order (get-param-str :str-order query-params)
                str-where (get-param-str :str-where query-params)
                show-graph (get-param-bool :show-graph query-params)]
            (set-str-renderer str-renderer)
            (set-str-draft-renderer str-renderer)
            (set-str-draft-order str-order)
            (set-str-order str-order)
            (set-str-draft-where str-where)
            (set-str-where str-where)
            (set-show-graph show-graph)
            (set-draft-show-graph show-graph)))
        fetch-device-logs (fn [str-where str-order]
                            (model.log/fetch-list
                             {:str-order str-order
                              :str-where str-where
                              :limit limit
                              :on-receive
                              (fn [logs total]
                                (set-logs logs)
                                (set-total total)
                                (set-logs-key (str str-where str-order limit)))}))
        on-click-apply
        (fn []
          (push-params {:str-renderer str-draft-renderer
                        :str-where str-draft-where
                        :str-order str-draft-order
                        :show-graph (str draft-show-graph)})
          (load-query-params))]
    (react/useEffect
     (fn []
       (load-query-params)
       (.addEventListener js/window "popstate" load-query-params)
       (fn [] ;; destructor
         (.removeEventListener js/window "popstate" load-query-params)))
     #js [])
    (react/useEffect
     (fn []
       (when-not (or (empty? str-where) (empty? str-order))
         (println "fetch logs" str-where str-order)
         (fetch-device-logs str-where str-order))
       (fn []))
     #js [str-where str-order])
    [:div
     [:h1 "device logs"]
     [:a.btn.btn-outline-primary.btn-sm.m-2 {:on-click #(set-show-config (not show-config))}
      (if show-config "hide config" "show config")]
     [:form.form-control {:style {:display (if show-config "block" "none")}}
      [render-textarea-json "renderer" str-renderer set-str-draft-renderer parse-error-config-renderer]
      [render-textarea-json "where" str-where set-str-draft-where parse-error-where]
      [render-textarea-json "order" str-order set-str-draft-order parse-error-order]
      [:div
       [render-checkbox "show graph" draft-show-graph set-draft-show-graph]]
      [:a.btn.btn-outline-primary.btn-sm {:on-click on-click-apply} "apply"]]
     (when show-graph
       [:f> log.graph/render-graphs logs-key logs config-renderer])
     [:div.m-1 "showing: " (count logs) ", total: " total]
     [:f> log.list/render-table-logs logs config-renderer]]))
