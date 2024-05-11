(ns front.view.login
  (:require ["react" :as react]
            [front.model.user :as user]
            ["react-router-dom" :as router]
            [front.route :as route]))

(defn render-text-input [{:keys [default set-val label type]}]
  [:<>
   [:label (:for label) label]
   [:input.form-control {:id label :type (or type "text") :default default
                         :on-change (fn [e] (set-val (-> e .-target .-value)))}]])

(defn core []
  (let [[email set-email] (react/useState)
        [password set-password] (react/useState)
        navigate (router/useNavigate)
        revalidator (router/useRevalidator)
        on-receive #(when-not (empty? %)
                      (do
                        (.revalidate revalidator)
                        (navigate route/dashboard)))]
    [:div
     [:div.row
      [:div.col-md-4.col-lg-4]
      [:form.col-md-4.col-lg-4
       [:h1 "Login"]
       #_[:div "signup is not open now"]
       [render-text-input {:set-val set-email :label "Email"}]
       [render-text-input {:set-val set-password :label "Password" :type "password"}]
       [:div.mt-2.align-right
        [:input.btn.btn-outline-primary
         {:type "button"
          :on-click #(user/login {:email email
                                  :password password
                                  :on-receive on-receive})
          :value "login"}]]]]]))
