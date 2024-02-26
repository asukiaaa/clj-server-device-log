(ns front.view.log.graph
  (:require [cljsjs.chartjs]
            ["react" :as react]
            [front.model.raw-device-log :as model.log]))

(defn render-graph-canvas [config graph-id]
  (react/useEffect
   (fn []
     (let [context (.getContext (.getElementById js/document graph-id) "2d")]
       (js/Chart. context (clj->js config)))
     (fn []))
   #js [])
  [:canvas {:id graph-id}])

(defn render-graph [logs val-config]
  (let [val-key (get val-config "key")
        config {:type "line"
                :data {:labels (for [log logs] (str (model.log/get-val-from-record log "created_at")))
                       :datasets [{:data (for [log logs] (model.log/get-val-from-record log val-key))
                                   :label (model.log/get-label-from-col-config val-config)
                                   :backgroundColor "#90EE90"}]}}]
    [:f> render-graph-canvas config (str "graph-" val-key)]))

(defn core [str-where str-order config-renderer config-renderer-graph]
  (let [[logs set-logs] (react/useState [])
        on-receive (fn [logs _total] (set-logs logs))]
    (react/useEffect
     (fn []
       (model.log/fetch-list {:where str-where :order str-order :on-receive on-receive})
       (fn []))
     #js [str-where str-order])

    [:<>
     (when-not (empty? logs)
       (for [val-config config-renderer]
         [:<> {:key val-config}
          [:f> render-graph logs val-config]]))]))
