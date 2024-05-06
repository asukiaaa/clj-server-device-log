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

;https://reactrouter.com/en/main/routers/create-browser-router
(def app-router
  (router/createBrowserRouter
   (clj->js [{:path "/"
              :element (r/as-element [:f> layout/core])
              :children
              [{:index true :element (r/as-element [:f> log.index/core])}
               {:path "front"
                :children
                [{:index true :element (r/as-element [:f> dashboard/core])}
                 {:path "login" :element (r/as-element [:f> login/core])}
                 {:path "*" :element (r/as-element [:div "page not found"]) :status 404}]}]}])))

(defonce root (rc/create-root (.getElementById js/document "app")))

(defn init []
  (println "init app")
  (rc/render root [:> router/RouterProvider {:router app-router}]))

(defn ^:dev/after-load re-render []
  (init))
