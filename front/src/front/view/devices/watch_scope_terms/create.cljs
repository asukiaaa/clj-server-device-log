(ns front.view.devices.watch-scope-terms.create
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]
            [front.model.device :as model.device]
            [front.model.watch-scope-term :as model.watch-scope-term]
            [front.model.watch-scope :as model.watch-scope]
            [front.model.util.watch-scope-term :as util.watch-scope-term]
            [front.util.timezone :as util.timezone]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        navigate (router/useNavigate)
        [device set-device] (react/useState)
        [watch-scope-list-and-total set-watch-scope-list-and-total] (react/useState)
        state-info-timezone (util/build-state-info :tiemzone react/useState {:default (util.timezone/get)})
        state-info-watch-scope-id (util/build-state-info :watch_scope_id react/useState)
        state-info-datetime-from (util/build-state-info :datetime_from react/useState)
        state-info-datetime-until (util/build-state-info :datetime_until react/useState)
        arr-state-info [state-info-watch-scope-id state-info-datetime-from state-info-datetime-until]
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive-response (fn [data errors]
                              (wrapper.fetching/set-errors info-wrapper-fetching errors)
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state arr-state-info]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data util.watch-scope-term/key-table :id)]
                                  (navigate (route/device-watch-scope-term-show id-device id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (let [params (reduce (fn [params state]
                                 (let [key-state (:key state)
                                       val (cond
                                             (some #(= % key-state) [:datetime_from :datetime_until])
                                             (util/build-str-datetime-from-draft state (:draft state-info-timezone))
                                             :else
                                             (:draft state))]
                                   (assoc params key-state val)))
                               {:device_id id-device} arr-state-info)]
            (println :params params)
            (model.watch-scope-term/create
             (merge params
                    {:on-receive on-receive-response}))))
        fetch-watch-scopes
        (fn [id-user-team errors next]
          (model.watch-scope/fetch-list-and-total-for-user-team
           id-user-team
           {:on-receive (fn [data new-errors]
                          (set-watch-scope-list-and-total data)
                          (next (concat errors new-errors)))}))
        fetch-device
        (fn [errors next]
          (model.device/fetch-by-id
           {:id id-device
            :on-receive (fn [item new-errors]
                          (set-device item)
                          (next (concat errors new-errors) item))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (fetch-device
        nil
        (fn [errors device]
          (fetch-watch-scopes
           (:user_team_id device)
           errors
           (fn [errors]
             (wrapper.fetching/finished info-wrapper-fetching errors)))))
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/devices) :path route/devices}
       {:label (util.label/device-item device)
        :path (route/device-show id-device)}
       {:label (util.label/term-of-watch-scope)
        :path (route/device-watch-scope-terms id-device)}
       {:label (util.label/create)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       [:div
        [:form.form-control
         [util/render-select (util.label/watch-scope) state-info-watch-scope-id
          (model.watch-scope/build-select-options-from-list-and-total watch-scope-list-and-total)]
         [:div (:draft state-info-timezone)]
         (util/render-datetime (util.label/start) state-info-datetime-from (:draft state-info-timezone))
         (util/render-datetime (util.label/end) state-info-datetime-until (:draft state-info-timezone))
         [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/create)]]]})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
