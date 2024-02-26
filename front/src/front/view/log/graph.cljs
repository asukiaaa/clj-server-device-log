(ns front.view.log.graph
  (:require [cljsjs.chartjs]
            ["react" :as react]))

(defn core []
  (let [data {:type "bar"
              :data {:labels ["2012" "2013" "2014" "2015" "2016"]
                     :datasets [{:data [5 10 15 20 25]
                                 :label "Rev in MM"
                                 :backgroundColor "#90EE90"}
                                {:data [3 6 9 12 15]
                                 :label "Cost in MM"
                                 :backgroundColor "#F08080"}]}}]
    (print "hoge")
    (react/useEffect
     (fn []
       (let [context (.getContext (.getElementById js/document "graph-sample") "2d")]
         (js/Chart. context (clj->js data)))
       (fn []))
     #js [])
    [:div
     [:p "graph page"]
     [:canvas {:id "graph-sample"}]]))
