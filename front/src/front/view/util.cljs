(ns front.view.util
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.view.util.label :as util.label]))

(def key-user-loggedin "user-loggedin")
(defn get-user-loggedin []
  (router/useRouteLoaderData key-user-loggedin))

(defn build-current-url-object []
  (->> js/window.location.href
       (new js/URL)))

(defn get-window-size []
  {:width (.-innerWidth js/window)
   :height (.-innerHeight js/window)})

(defn assign-arr-state-info-to-params [arr-info-state params]
  (reduce (fn [params info-state]
            (assoc params (:key info-state) (:draft info-state)))
          params arr-info-state))

(defn push-query-params [query-params]
  ;; (println "push-url")
  (let [url-object (build-current-url-object)
        url-search-params (.-searchParams url-object)]
    (doseq [[k v] query-params]
      #_(println "in for" k v)
      (.set url-search-params (name k) v))
    ;; (println "url is" url)
    ;; (println "url-object" url-object)
    ;; (println "url-search-params" url-search-params)
    (js/history.pushState nil nil (str "?" url-search-params))))

(defn read-query-params []
  ;; (println "read-params")
  (let [url-object (build-current-url-object)
        url-search-params (.-searchParams url-object)]
    ;; (println "url is" url)
    ;; (println "url-object" url-object)
    ;; (println "url-search-params" url-search-params)
    ;; (println "cljs params" (js->clj url-search-params))
    (into {} (for [key (.keys url-search-params)]
               [(keyword key) (.get url-search-params key)]))))

(defn build-state-info [key build-state & [{:keys [default draft]}]]
  (let [value-default default
        value-draft (or draft default)]
    (let [state-default (if value-default (build-state value-default) (build-state))
          state-draft (if value-draft (build-state value-draft) (build-state))
          state-errors (build-state)]
      {:key key
       :default (first state-default)
       :set-default (second state-default)
       :draft (first state-draft)
       :set-draft (second state-draft)
       :errors (first state-errors)
       :set-errors (second state-errors)})))

(defn set-default-and-draft [info val]
  ((:set-default info) val)
  ((:set-draft info) val))

(defn render-errors-as-alerts [errors]
  (when errors
    [:<>
     (for [error errors]
       [:div.alert.alert-danger {:key error} error])]))

(defn render-errors-for-input [errors]
  (when errors
    [:<>
     (for [error errors]
       [:div.invalid-feedback {:key error} error])]))

(defn render-textarea [label {:keys [default set-draft errors]} & [{:keys [disabled]}]]
  [:div
   [:div label]
   [:textarea.form-control.mb-1
    {:type :text :default-value default :key default :class (when errors :is-invalid)
     :disabled disabled
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}]
   (render-errors-for-input errors)])

(defn render-checkbox [label {:keys [draft set-draft]}]
  [:span
   [:input.p-2
    {:id label
     :type "checkbox"
     :checked (= "true" draft)
     :on-change (fn [] (set-draft (if (= "true" draft) "false" "true")))}]
   [:label.p-2 {:for label} label]])


(defn build-item-values [{:keys [default key draft errors]} keys-assoc-in]
  {:item-default (if keys-assoc-in (get-in default keys-assoc-in) default)
   :item-draft (if keys-assoc-in (get-in draft keys-assoc-in) draft)
   :item-errors  (if keys-assoc-in (get-in errors keys-assoc-in) errors)
   :item-key (if keys-assoc-in (str keys-assoc-in) key)})

(defn assign-to-draft [value {:keys [set-draft draft]} keys-assoc-in]
  (set-draft (if keys-assoc-in
               (assoc-in draft keys-assoc-in value)
               value)))

(defn render-input [label state-info {:keys [type str-class-wrapper str-class-input disabled keys-assoc-in]}]
  (let [{:keys [item-default item-draft item-errors item-key]} (build-item-values state-info keys-assoc-in)]
    #_[:input.form-control
       {:id label
        :name item-key
        :class (str str-class-input " " (when item-errors "is-invalid"))
        :value (or item-draft item-default)
        :disabled disabled
        :type type
        :on-change
        (fn [e]
          (let [value (-> e .-target .-value)]
            (assign-to-draft value state-info keys-assoc-in)))}]
    [:div {:class str-class-wrapper}
     (when label
       [:div
        [:label {:for item-key} label]])
     [:input.form-control
      {:id label
       :name item-key
       :class (str str-class-input " " (when item-errors "is-invalid"))
       :value (or item-draft item-default)
       :disabled disabled
       :type type
       :on-change
       (fn [e]
         (let [value (-> e .-target .-value)]
           (assign-to-draft value state-info keys-assoc-in)))}]
     (render-errors-for-input item-errors)]))

(defn render-select [label state-info value-labels & [{:keys [type str-class-wrapper disabled on-blur keys-assoc-in override-on-change without-empty-option]}]]
  (let [{:keys [item-default item-draft item-errors item-key]} (build-item-values state-info keys-assoc-in)]
    [:div {:class str-class-wrapper}
     [:div
      [:label {:for item-key} label]]
     [:select.form-control
      {:id label
       :name item-key
       :disabled disabled
       :class (when item-errors :is-invalid)
       :default-value item-default
       :type type
       :on-blur #(when on-blur (on-blur))
       :on-change
       (fn [e]
         (let [value (-> e .-target .-value)]
           (if override-on-change
             (override-on-change value state-info keys-assoc-in)
             (assign-to-draft value state-info keys-assoc-in))))}
      (when-not without-empty-option
        [:option ""])
      (let [value-selected (or item-draft item-default)]
        (for [[value label] value-labels]
          [:option {:value value :key value} label (= value value-selected)]))]
     (render-errors-for-input item-errors)]))

(defn btn-confirm-delete [{:keys [message-confirm action-delete]}]
  [:a {:on-click
       (fn [e]
         (.preventDefault e)
         (when (js/confirm message-confirm) (action-delete)))
       :href ""}
   util.label/delete])

(defn copy-to-clipboard [text]
  (-> js/navigator .-clipboard (.writeText text)))

(defn button-to-copy-to-clipboard [text]
  (let [[clicked set-clicked] (react/useState)
        on-click
        (fn [e]
          (.preventDefault e)
          (set-clicked true)
          (copy-to-clipboard text))]
    [:<>
     (if clicked
       [:span util.label/copied]
       [:button.btn.btn-secondary.btn-sm {:on-click on-click} util.label/copy])]))

(defn button-to-fetch-authorization-bearer [fetch-authorization-bearer & [{:keys [on-fetched]}]]
  (let [[fetching set-fetching] (react/useState)
        [bearer set-bearer] (react/useState)
        [show set-show] (react/useState)
        on-click
        (fn []
          (set-fetching true)
          (fetch-authorization-bearer
           {:on-receive
            (fn [item errors]
              (let [bearer (:authorization_bearer item)]
                (set-bearer bearer)
                (set-fetching false)
                (when on-fetched (on-fetched bearer errors))))}))]
    (if bearer
      [:div
       [:span.me-1
        (if show
          [:button.btn.btn-secondary.btn-sm {:on-click #(set-show false)} util.label/hide]
          [:button.btn.btn-secondary.btn-sm {:on-click #(set-show true)} util.label/show])]
       [:f> button-to-copy-to-clipboard bearer]
       (when show
         [:div bearer])]
      [:button.btn.btn-secondary.btn-sm
       {:on-click on-click :disabled fetching}
       util.label/get-bearer])))
