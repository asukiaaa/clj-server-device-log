(ns front.view.login
  (:require ["react" :as react]
            [front.model.user :as user]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.view.util :as util]
            [front.view.util.label :as util.label]))

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
        state-info-show-password (util/build-state-info :show-password #(react/useState))
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
     [:div.row.m-0
      [:div.col-md-3]
      [:form.col-md-6.px-0.pt-2
       [util/area-content
        [:h1 (util.label/login)]
        [util/render-errors-as-alerts errors]
        [util/render-input (util.label/email) state-info-email {:disabled waiting-response}]
        (let [show-password (:draft state-info-show-password)
              type-for-password (if (= show-password "true") :text :password)]
          [:<>
           [util/render-input (util.label/password) state-info-password
            {:type type-for-password :disabled waiting-response}]])
        [:div [util/render-checkbox (util.label/show-password) state-info-show-password]]
        [:div.mt-2.align-right
         [:button.btn.btn-outline-primary
          {:on-click on-click-login :class (when waiting-response :disabled)}
          (util.label/login)]]]]]]))
