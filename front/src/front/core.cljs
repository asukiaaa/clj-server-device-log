(ns front.core
  (:require [reagent.dom :as dom]
            [re-graph.core :as re-graph]
            [front.view.log.index :as log.index]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

(defn component-app []
  [:div
   [:f> log.index/core]])

(dom/render [component-app] (.getElementById js/document "app"))

(defn init []
  (println "init shadowjs"))
