(ns front.core
  (:require [reagent.dom :as dom]
            ["react" :as react]
            [re-graph.core :as re-graph]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

(defn component-device-log [log]
  (let [[requested-to-open set-requested-to-open] (react/useState false)
        id (:id log)
        window-width (. js/window -innerWidth)]
    [:<>
     [:tr
      [:td id]
      [:td (:created_at log)]
      [:td [:a ;; .btn.btn-outline-primary.btn-sm
            {:href "#"
             :on-click (fn [e]
                         (.preventDefault e)
                         (set-requested-to-open (not requested-to-open)))}
            (if requested-to-open "close" "open")]]]
     (when requested-to-open
       [:tr
        [:td {:colSpan 3}
         [:pre {:style {:overflow :auto :max-width (- window-width 30)}}
          (.stringify js/JSON (.parse js/JSON (:data log)) nil 2)]]])]))

(defn component-device-logs []
  (let [[logs set-logs] (react/useState nil)
        on-receive (fn [{:keys [data]}]
                     (set-logs (-> data :raw_device_logs :list)))]
    (println "render-component-device-logs")
    (react/useEffect
     #(re-graph/query "{ raw_device_logs { total list { id created_at data } } }" {} on-receive)
     #())
    [:div
     [:h1 "device logs"]
     [:table.table.table-sm
      [:thead
       [:tr
        [:th "id"]
        [:th "created_at"]
        [:th "actions"]]]
      [:tbody
       (for [log logs]
         [:<> {:key (:id log)}
          [:f> component-device-log log]])]]]))

(defn component-app []
  [:div
   [:f> component-device-logs]])

(dom/render [component-app] (.getElementById js/document "app"))
