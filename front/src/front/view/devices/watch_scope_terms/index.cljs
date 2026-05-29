(ns front.view.devices.watch-scope-terms.index
  (:require ["react" :as react]
            ["react-router-dom" :as router]
            [front.route :as route]
            [front.model.watch-scope-term :as model.watch-scope-term]
            [front.model.util.device :as m.util.device]
            [front.view.common.wrapper.show404 :as wrapper.show404]
            [front.view.util.breadcrumb :as breadcrumb]
            [front.view.util.label :as util.label]
            [front.view.util.table :as util.table]
            [front.view.util.watch-scope :as util.watch-scope]
            [front.view.util :as util]))

(defn render-item [watch-scope-term]
  [:tr
   [:td (let [watch-scope (:watch_scope watch-scope-term)]
          [:> router/Link {:to (route/watch-scope-show (:id watch-scope))}
           (:name watch-scope)])]
   [:td (util.watch-scope/render-term-from-to watch-scope-term)]
   [:td
    #_(util/render-list-inline
       (v.watch-scope.util/build-related-links watch-scope))]])

(def labels-header [(util.label/watch-scope) (util.label/term) (util.label/action)])

(defn- page []
  (let [params (js->clj (router/useParams))
        id-device (get params "device_id")
        [device set-device] (react/useState)
        on-receive
        (fn [data _errors]
          (set-device (m.util.device/key-table data)))
        fetch-list-and-total
        (fn [params]
          (model.watch-scope-term/fetch-list-and-total-for-device
           (assoc params :id-device id-device)))]
    [:<>
     [:f> breadcrumb/core
      [{:label (util.label/devices) :path route/devices}
       {:label (util.label/device-item device) :path (route/device-show id-device)}
       {:label (util.label/term)}]]
     [util/area-content
      [:> router/Link {:to (route/device-watch-scope-term-create id-device)} (util.label/create)]]
     [:f> util.table/core fetch-list-and-total labels-header render-item {:on-receive on-receive}]]))

(defn core []
  (wrapper.show404/wrapper
   {:permission wrapper.show404/permission-login
    :page page}))
