(ns reagent-practice.core
  (:require #_[goog.dom :as gdom]
            #_react-dom
            [reagent.dom :as dom]))

#_(println "hello world")

(defn simple-component []
  [:div
   [:p "I am a component!"]
   [:p.someclass
    "I have " [:strong "bold"]
    [:span {:style {:color "red"}} " and red "] "text."]])

(dom/render [simple-component] (.getElementById js/document "app"))
