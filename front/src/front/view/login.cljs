(ns front.view.login
  (:require ["react" :as react]))

(defn render-text-input [{:keys [default set-val label type]}]
  [:<>
   [:label (:for label) label]
   [:input.form-control {:id label :type (or type "text") :default default
                         :on-change (fn [e] (set-val (-> e .-target .-value)))}]])

(defn core []
  (let [[email set-email] (react/useState)
        [password set-password] (react/useState)
        login (fn [] (println email password))]
    [:div
     [:div.row
      [:div.col-md-4.col-lg-4]
      [:form.col-md-4.col-lg-4
       [:h1 "Login"]
       #_[:div "signup is not open now"]
       [render-text-input {:set-val set-email :label "Email"}]
       [render-text-input {:set-val set-password :label "Password" :type "password"}]
       [:div.mt-2.align-right
        [:input.btn.btn-outline-primary {:type "button" :on-click login :value "login"}]]]]]))
