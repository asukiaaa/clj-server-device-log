(ns front.view.watch-scopes.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.user-team :as model.user-team]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]
            [front.model.device :as model.device]))

(defn render-fields-for-term [state-info-terms index options-for-device-ids on-click-delete]
  (let [term (get (:draft state-info-terms) index)]
    [:div.row
     [:div.col-sm
      [util/render-select util.label/device state-info-terms options-for-device-ids
       {:keys-assoc-in [index :device_id]}]]
     [:div.col-sm
      [util/render-input util.label/from state-info-terms
       {:keys-assoc-in [index :datetime_from_date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index :datetime_from_time]
        :disabled (empty? (:datetime_from_date term))
        :type "time"}]]
     [:div.col-sm
      [util/render-input util.label/until state-info-terms
       {:keys-assoc-in [index :datetime_until_date]
        :type "date"}]
      [util/render-input util.label/time state-info-terms
       {:keys-assoc-in [index :datetime_until_time]
        :disabled (empty? (:datetime_until_date term))
        :type "time"}]]
     [:div.col-sm
      [:div util.label/action]
      [:a.btn.btn-light {:href "#"
                         :on-click (fn [e]
                                     (.preventDefault e)
                                     (on-click-delete))}
       util.label/delete]]]))

(defn build-datetime-from-str-date-and-time [str-date str-time]
  (when-not (empty str-date)
    (if (empty? str-time)
      str-date
      (str str-date " " str-time))))

(defn terms-input->params [terms]
  (for [term terms]
    (merge term
           {:datetime_from (build-datetime-from-str-date-and-time
                            (:datetime_from_date term)
                            (:datetime_from_time term))
            :datetime_until (build-datetime-from-str-date-and-time
                             (:datetime_until_date term)
                             (:datetime_until_time term))})))

(defn- page []
  (let [navigate (router/useNavigate)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-id-user-team (util/build-state-info :user_team_id #(react/useState))
        state-info-terms (util/build-state-info :terms #(react/useState []))
        [devices-list-and-total set-devices-list-and-total] (react/useState)
        [user-team-list-and-total set-user-team-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive
        (fn [data errors]
          (when errors ((:set-errors state-info-system) errors))
          (if-let [errors-str (:errors data)]
            (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
              (doseq [state [state-info-system state-info-name state-info-id-user-team]]
                (let [key (:key state)
                      errors-for-key (get errors key)]
                  ((:set-errors state) errors-for-key))))
            (when-let [id (-> data (get (keyword model.watch-scope/name-table)) :id)]
              (navigate (route/watch-scope-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.watch-scope/create
           {:name (:draft state-info-name)
            :terms (terms-input->params (:draft state-info-terms))
            :user_team_id (:draft state-info-id-user-team)
            :on-receive on-receive}))
        load-list
        (fn []
          (wrapper.fetching/start info-wrapper-fetching)
          (model.user-team/fetch-list-and-total
           {:limit 10000 ; TODO apply search
            :page 0
            :on-receive (fn [result errors]
                          (set-user-team-list-and-total result)
                          (wrapper.fetching/finished info-wrapper-fetching errors))}))
        load-devices
        (fn []
          (model.device/fetch-list-and-total-for-user-team
           {:limit 1000
            :user_team_id (:draft state-info-id-user-team)
            :page 0
            :on-receive
            (fn [result errors]
              (set-devices-list-and-total result))}))
        add-term
        (fn [e]
          (.preventDefault e)
          ((:set-draft state-info-terms) (conj (:draft state-info-terms) {}))
          ((:set-default state-info-terms) (conj (:default state-info-terms) {})))
        delete-term
        (fn [index]
          (let [terms-draft (:draft state-info-terms)
                terms-default (:default state-info-terms)]
            ((:set-draft state-info-terms) (into (subvec terms-draft 0 index) (subvec terms-draft (inc index))))
            ((:set-default state-info-terms) (when terms-default (into (subvec terms-default 0 index) (subvec terms-default (inc index)))))))]
    (react/useEffect
     (fn []
       (load-list)
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label util.label/watch-scopes :path route/watch-scopes}
                           {:label util.label/create}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-errors-as-alerts (:errors state-info-system)]
         [util/render-input util.label/name state-info-name]
         [util/render-select util.label/user-team state-info-id-user-team
          (model.user-team/build-select-options-from-list-and-total user-team-list-and-total)
          {:on-blur load-devices}]
         [:div
          [:div util.label/terms]
          (let [options-for-device-ids (model.device/build-select-options-from-list-and-total devices-list-and-total)]
            (for [[index _] (map-indexed vector (:draft state-info-terms))]
              [:<> {:key index}
               (render-fields-for-term state-info-terms index options-for-device-ids (fn [] (delete-term index)))]))
          [:a.btn.btn-light.mt-1 {:href "#" :on-click add-term} util.label/add-term]]
         [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/create]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
