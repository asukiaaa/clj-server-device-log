(ns front.view.util
  (:require ["react-router-dom" :as router]))

(def key-user-loggedin "user-loggedin")
(defn get-user-loggedin []
  (router/useRouteLoaderData key-user-loggedin))

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

(defn render-errors [errors]
  (when errors
    (for [error errors]
      [:div.invalid-feedback {:key error} error])))

(defn render-textarea [label {:keys [default set-draft errors]} error-message]
  [:div
   [:div label]
   [:div (str error-message)]
   [:textarea.form-control.mb-1
    {:type :text :default-value default :key default :class (when errors :is-invalid)
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}]
   (render-errors errors)])

(defn render-checkbox [label {:keys [draft set-draft]}]
  [:span
   [:input.p-2
    {:id label
     :type "checkbox"
     :checked (= "true" draft)
     :on-change (fn [] (set-draft (if (= "true" draft) "false" "true")))}]
   [:label.p-2 {:for label} label]])

(defn render-input [label {:keys [key draft set-draft errors]} {:keys [type wrapper-class]}]
  [:div {:class wrapper-class}
   [:div
    [:label {:for key} label]]
   [:input.form-control
    {:id label
     :class (when errors :is-invalid)
     :value draft
     :type type
     :on-change (fn [e] (set-draft (-> e .-target .-value)))}]
   (when errors
     (for [error errors]
       [:div.invalid-feedback {:key error} error]))])
