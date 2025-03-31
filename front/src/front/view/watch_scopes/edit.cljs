(ns front.view.watch-scopes.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util :as util]
            [front.view.watch-scopes.util :as v.watch-scope.util]
            [front.model.user-team :as model.user-team]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "watch_scope_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        [user-team set-user-team] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        state-info-terms (util/build-state-info :terms #(react/useState []))
        [device-list-and-total set-device-list-and-total] (react/useState)
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        load-devices
        (fn [id-user-team next]
          (model.device/fetch-list-and-total-for-user-team
           {:limit 1000
            :user_team_id id-user-team
            :page 0
            :on-receive
            (fn [result errors]
              (set-device-list-and-total result)
              (next errors))}))
        on-receive-item
        (fn [item]
          (set-item item)
          (set-user-team (model.user-team/key-table item))
          (util/set-default-and-draft state-info-name (:name item))
          (util/set-default-and-draft state-info-terms (util.watch-scope/terms-params->draft (:terms item))))
        on-receive-response (fn [data errors]
                              (when errors ((:set-errors state-info-system) errors))
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-name state-info-system]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data (get (keyword model.watch-scope/name-table)) :id)]
                                  (navigate (route/watch-scope-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (model.watch-scope/update
           {:id id-item
            :name (:draft state-info-name)
            :terms (util.watch-scope/terms-draft->params (:draft state-info-terms))
            :on-receive on-receive-response}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.watch-scope/fetch-by-id
        {:id id-item
         :on-receive
         (fn [item errors]
           (on-receive-item item)
           (if-not (empty? errors)
             (wrapper.fetching/finished info-wrapper-fetching errors)
             (load-devices
              (:user_team_id item)
              (fn [errors]
                (wrapper.fetching/finished info-wrapper-fetching errors)))))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/watch-scopes) :path route/watch-scopes}
       {:label (util.label/watch-scope-item item) :path (route/watch-scope-show id-item)}
       {:label (util.label/edit)}]]
     (util/render-list-in-area-content-line
      (v.watch-scope.util/build-related-links item {:id-item id-item}))
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-input (util.label/name) state-info-name]
           [:div
            [:div (util.label/user-team)]
            [:div (util.label/user-team-item user-team)]]
           (util.watch-scope/render-fields-for-terms state-info-terms device-list-and-total)
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
