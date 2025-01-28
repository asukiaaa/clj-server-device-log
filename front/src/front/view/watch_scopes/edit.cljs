(ns front.view.watch-scopes.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.label :as util.label]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "watch_scope_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-name (util/build-state-info :name #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-name (:name item)))
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
            :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.watch-scope/fetch-by-id {:id id-item
                                       :on-receive (fn [user errors]
                                                     (on-receive-item user)
                                                     (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label util.label/watch-scopes :path route/watch-scopes}
       {:label (util.label/watch-scope-item item)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div "no data"]
         [:div
          [:form.form-control
           [util/render-errors-as-alerts (:errors state-info-system)]
           [util/render-input util.label/name state-info-name]
           [:button.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} util.label/update]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
