(ns front.core
  (:require [reagent.dom :as dom]
            [re-graph.core :as re-graph]
            [front.views.logs.list :as logs.list]
            [front.views.logs.graph :as logs.graph]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

(defn component-app []
  [:div
   (let [path (aget js/window "location" "pathname")]
     (if (= path "/graph")
       [:f> logs.graph/core]
       [:f> logs.list/core]))])

(dom/render [component-app] (.getElementById js/document "app"))
