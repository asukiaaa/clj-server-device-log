(ns front.view.devices.watch-scope-terms.edit
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [clojure.walk :refer [keywordize-keys]]
            [front.route :as route]
            [front.model.device :as model.device]
            [front.model.watch-scope :as model.watch-scope]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.common.wrapper.fetching :as wrapper.fetching]
            [front.view.util :as util]))

(defn- page []
  (let [params (js->clj (router/useParams))
        id-item (get params "device_id")
        navigate (router/useNavigate)
        [watch-scope-list-and-total set-watch-scope-list-and-total] (react/useState)
        [item set-item] (react/useState)
        state-info-terms (util/build-state-info :terms #(react/useState []))
        arr-state-info [state-info-terms]
        on-receive-item
        (fn [item]
          (set-item item)
          (doseq [state arr-state-info]
            (util/set-default-and-draft state ((:key state) item))))
        info-wrapper-fetching (wrapper.fetching/build-info #(react/useState))
        on-receive-response (fn [data errors]
                              (wrapper.fetching/set-errors info-wrapper-fetching errors)
                              (if-let [errors-str (:errors data)]
                                (let [errors (keywordize-keys (js->clj (.parse js/JSON errors-str)))]
                                  (doseq [state arr-state-info]
                                    (let [key (:key state)
                                          errors-for-key (get errors key)]
                                      ((:set-errors state) errors-for-key))))
                                (when-let [id (-> data :device :id)]
                                  (navigate (route/device-show id)))))
        on-click-apply
        (fn [e]
          (.preventDefault e)
          (let [params (reduce (fn [params state]
                                 (assoc params (:key state) (:draft state)))
                               nil arr-state-info)]
            (model.device/update
             (merge params
                    {:id id-item
                     :on-receive on-receive-response}))))
        fetch-watch-scopes
        (fn [errors next]
          (model.watch-scope/fetch-list-and-total
           {:limit 1000
            :page 0
            :on-receive (fn [list-and-total new-errors]
                          (set-watch-scope-list-and-total list-and-total)
                          (next (concat errors new-errors)))}))
        fetch-device
        (fn [errors next]
          (model.device/fetch-by-id
           {:id id-item
            :on-receive (fn [user new-errors]
                          (on-receive-item user)
                          (next (concat errors new-errors)))}))]
    (react/useEffect
     (fn []
       (wrapper.fetching/start info-wrapper-fetching)
       (fetch-device
        nil
        (fn [errors]
          (fetch-watch-scopes
           errors
           (fn [errors]
             (wrapper.fetching/finished info-wrapper-fetching errors)))))
       (fn []))
     #js [])
    [:<>
     [:f> breadcrumb/core [{:label (util.label/devices) :path route/devices}
                           {:label (util.label/device-item item)
                            :path (when item (route/device-show id-item))}
                           {:label (util.label/watch-scope)}
                           {:label (util.label/edit)}]]
     (wrapper.fetching/wrapper
      {:info info-wrapper-fetching
       :renderer
       (if (empty? item)
         [:div (util.label/no-data)]
         [:div
          [:form.form-control
           (util.watch-scope/render-fields-for-terms-refer-watch-scope state-info-terms watch-scope-list-and-total)
           [:button.btn.btn-primary.mt-1 {:on-click on-click-apply} (util.label/update)]]])})]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-admin
    :page page}))
