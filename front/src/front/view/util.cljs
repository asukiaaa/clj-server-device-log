(ns front.view.util
  (:require ["react-router-dom" :as router]))

(def key-user-loggedin "user-loggedin")
(defn get-user-loggedin []
  (router/useRouteLoaderData key-user-loggedin))

(defn push-query-params [query-params]
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

(defn read-query-params []
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

(defn build-state-info [key build-state]
  (let [state-default (build-state)
        state-draft (build-state)
        state-errors (build-state)]
    {:key key
     :default (first state-default)
     :set-default (second state-default)
     :draft (first state-draft)
     :set-draft (second state-draft)
     :errors (first state-errors)
     :set-errors (second state-errors)}))

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

(defn render-textarea [label {:keys [default set-draft errors]} error-message]
  [:div
   [:div label]
   [:div (str error-message)]
   [:textarea.form-control.mb-1
    {:type :text :default-value default :key default :class (when errors :is-invalid)
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

(defn render-input [label {:keys [default key draft set-draft errors]} {:keys [type wrapper-class]}]
  [:div {:class wrapper-class}
   [:div
    [:label {:for key} label]]
   [:input.form-control
    {:id label
     :class (when errors :is-invalid)
     :value (or draft default)
     :type type
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}]
   (when errors
     (for [error errors]
       [:div.invalid-feedback {:key error} error]))])

(defn render-select [label {:keys [default key draft set-draft errors]} value-labels {:keys [type wrapper-class]}]
  [:div {:class wrapper-class}
   [:div
    [:label {:for key} label]]
   [:select.form-control
    {:id label
     :name key
     :class (when errors :is-invalid)
     :default-value (or draft default)
     :type type
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}
    [:option ""]
    (let [value-selected (or draft default)]
      (for [[value label] value-labels]
        [:option {:value value :key value} label (= value value-selected)]))]
   (when errors
     (for [error errors]
       [:div.invalid-feedback {:key error} error]))])

(defn btn-confirm-delete [{:keys [message-confirm action-delete]}]
  [:a {:on-click
       (fn [e]
         (.preventDefault e)
         (when (js/confirm message-confirm) (action-delete)))
       :href ""}
   "delete"])
