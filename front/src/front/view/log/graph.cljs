(ns front.view.log.graph
  (:require
   [cljsjs.chartjs]
   [cljsjs.chartjs-adapter-date-fns]
   [cljsjs.date-fns]
   #_"chart.js"
   #_["chart.js" :as chart]
   #_["chart.js" :refer (Chart)]
   #_"moment"
   #_"chartjs-adapter-moment"
   #_["chartjs-adapter-moment" :as adapter]
   #_["chart.js/auto" :refer [Chart]]
   ["react" :as react]
   #_["date-fns/locale/index.js" :refer [ja]]
   [front.model.raw-device-log :as model.log]))

#_(js/require "chart.js")
#_(js/require "moment")
#_(js/require "chartjs-adapter-moment")

#_(def chart (js/require "chart.js/auto"))
#_(js/require "chartjs-adapter-date-fns")
#_(.registerer chart/Chart (chart/registerers))
#_(println (chart/Chart.register (... chart/registerables)))
#_(println (chart/registerables js/...))
#_(doseq [r chart/registerables] #_(println r) (.register chart/Chart r))
#_(.register chart/Chart chart/TimeScale)
#_(.log js/console adapter)
#_(println chart/registerables #_(first))

#_(.log js/console "adapters")
#_(println chart/_adapters)
#_(.log js/console (.-_date chart/_adapters))

#_(def  values [{:x "2020-02-01" :y 100}
                {:x "2020-02-02" :y 200}
                {:x "2020-02-03" :y 300}
                {:x "2020-02-04" :y 400}
                {:x "2020-02-07" :y 200}
                {:x "2020-02-14" :y 600}])

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
        config #_{:type "line"
                  :data {:datasets [{:data values
                                     :backgroundColor "#90EE90"}]}
                  :options
                  {:scales
                   {:x
                    {:type "time"
                     :time {:unit "day"
                            :displayFormats {:week "yyyy-MM-dd"}}}}}}
        {:type "line"
         :data {#_:labels #_[] #_(for [log logs] (str (model.log/get-val-from-record log "created_at")))
                :datasets [{:data (for [log logs] #_(model.log/get-val-from-record log val-key)
                                       {:x #_"2024-120-20" (:created_at log)
                                        :y (model.log/get-val-from-record log val-key)})
                            :label (model.log/get-label-from-col-config val-config)
                            :backgroundColor "#90EE90"}]}
         :options
         {:scales
          {:x
           {:type "time"
            :time {:unit nil
                   :displayFormats {:hour "HH"
                                    :week "MM/dd"
                                    :day "MM/dd"
                                    :month "yyyy/MM"}}}}}}]
    [:f> render-graph-canvas config (str "graph-" val-key)]))

(defn core [str-where str-order config-renderer config-renderer-graph]
  (let [[logs set-logs] (react/useState [])
        on-receive (fn [logs _total] (set-logs logs))]
    (react/useEffect
     (fn []
       (model.log/fetch-list {:where str-where :order str-order :on-receive on-receive})
       (fn []))
     #js [str-where str-order])
    [:div.container-fluid {:key (str str-where str-order config-renderer)}
     (when-not (empty? logs)
       [:div.row
        (for [val-config config-renderer]
          [:<> {:key val-config}
           [:div.col-md-6
            [:f> render-graph logs val-config]]])])]))
