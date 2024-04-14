(ns front.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rc]
            [re-graph.core :as re-graph]
            [front.view.log.index :as log.index]
            [front.view.login :as login]
            [front.view.dashboard :as dashboard]
            [front.view.layout :as layout]
            ["react-router-dom" :as router]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

(defn page-user-show []
  (let [params (js->clj (router/useParams))
        id (get params "id")]
    [:div "user show " (str id)]))

; https://github.com/remix-run/react-router/blob/dev/examples/route-objects/src/App.tsx
#_(defn component-app []
    (let  [routes
           [{:path "/"
             :children
             [{:index true :element (r/as-element [:f> log.index/core])}
              {:path "/front"
               :children
               [{:path "/login" :element (r/as-element [:f> page-login])}]}]}]
           element (router/useRoutes #_routes (clj->js routes))]
      element #_[:div "hoge" element []]))

; https://github.com/remix-run/react-router/blob/dev/examples/basic/src/App.tsx
(defn component-app []
  [:> router/Routes
   [:> router/Route {:path "/" :element (r/as-element [:f> layout/core])}
    [:> router/Route {:index true :element (r/as-element [:f> log.index/core])}]
    [:> router/Route {:path "/front"}
     [:> router/Route {:index true :element (r/as-element [:f> dashboard/core])}]
     [:> router/Route {:path "login" :element (r/as-element [:f> login/core])}]
     [:> router/Route {:path "users/:id" :element (r/as-element [:f> page-user-show])}]
     [:> router/Route {:path "*" :element (r/as-element [:div "page not found"]) :status 404}]]]])

(defonce root (rc/create-root (.getElementById js/document "app")))

(defn init []
  (println "init app")
  (rc/render root
             #_[:f> log.index/core]
             [:> router/BrowserRouter
              [component-app]]))

(defn ^:dev/after-load re-render []
  (init))
