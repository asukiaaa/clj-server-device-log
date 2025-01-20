(ns front.view.login
  (:require ["react" :as react]
            [front.model.user :as user]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util :as util]))

(defn render-text-input [{:keys [default set-val label type]}]
  [:<>
   [:label (:for label) label]
   [:input.form-control {:id label :type (or type "text") :default default
                         :on-change (fn [e] (set-val (-> e .-target .-value)))}]])

(defn core []
  (let [[search-params _set-search-params] (router/useSearchParams)
        path-afetr-login (or (.get search-params "path_after_login") route/dashboard)
        state-info-email (util/build-state-info :email #(react/useState))
        state-info-password (util/build-state-info :password #(react/useState))
        [errors set-errors] (react/useState)
        [waiting-response set-waiting-response] (react/useState)
        navigate (router/useNavigate)
        revalidator (router/useRevalidator)
        on-receive (fn [data]
                     (set-waiting-response false)
                     (if (empty? data)
                       (set-errors ["email or password unmatch"])
                       (do
                         (.revalidate revalidator)
                         (navigate path-afetr-login))))
        on-click-login (fn [e]
                         (.preventDefault e)
                         (set-errors nil)
                         (set-waiting-response true)
                         (user/login {:email (:draft state-info-email)
                                      :password (:draft state-info-password)
                                      :on-receive on-receive}))]
    [:div
     [:div.row
      [:div.col-md-4.col-lg-4]
      [:form.col-md-4.col-lg-4
       [:h1 "Login"]
       [util/render-errors-as-alerts errors]
       [util/render-input "Email" state-info-email {:disabled waiting-response}]
       [util/render-input "Password" state-info-password {:type :password :disabled waiting-response}]
       [:div.mt-2.align-right
        [:button.btn.btn-outline-primary
         {:on-click on-click-login :class (when waiting-response :disabled)}
         "login"]]]]]))
