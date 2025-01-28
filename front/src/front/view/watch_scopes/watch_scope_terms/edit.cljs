(ns front.view.watch-scopes.watch-scope-terms.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.watch-scope-term :as model.watch-scope-term]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util :as util]
            [front.view.common.wrapper.fetching :as wrapper.fetching]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-watch-scope (get params "watch_scope_id")
        id-watch-scope-term (get params "watch_scope_term_id")
        navigate (router/useNavigate)
        [item set-item] (react/useState)
        state-info-system (util/build-state-info :__system #(react/useState))
        state-info-display-name (util/build-state-info :name #(react/useState))
        on-receive-item
        (fn [item]
          (set-item item)
          (util/set-default-and-draft state-info-display-name (:name item)))
        on-receive-response (fn [data]
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state [state-info-system state-info-display-name]]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :watch_scope_term :id)]
                                  (navigate (route/watch-scope-watch-scope-term-show id-watch-scope id)))))
        on-click-apply (fn [] (model.watch-scope-term/update
                               {:id id-watch-scope-term
                                :display-name (:draft state-info-display-name)
                                :on-receive on-receive-response}))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (model.watch-scope-term/fetch-by-id-for-watch-scope
        {:id-watch-scope id-watch-scope
         :id id-watch-scope-term
         :on-receive (fn [user errors]
                       (on-receive-item user)
                       (wrapper.fetching/finished info-wrapper-fetching errors))})
       (fn []))
     #js [])
    (wrapper.fetching/wrapper
     {:info info-wrapper-fetching
      :renderer
      (if (empty? item)
        [:div "no data"]
        [:div
         [:h1.h3.mx-2 "edit device watch group device"]
         [:form.form-control
          [util/render-errors-as-alerts (:errors state-info-system)]
          [util/render-input "name device" state-info-display-name]
          [:a.btn.btn-primary.btn-sm.mt-1 {:on-click on-click-apply} "apply"]]])})))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
