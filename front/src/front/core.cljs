(ns front.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rc]
            [re-graph.core :as re-graph]
            [front.view.log.index :as log.index]
            [front.view.login :as login]
            [front.view.dashboard :as dashboard]
            [front.view.layout :as layout]
            [front.view.users.index :as user.index]
            [front.view.users.create :as user.create]
            [front.view.users.show :as user.show]
            [front.view.users.edit :as user.edit]
            [front.view.devices.index :as device.index]
            [front.view.devices.create :as device.create]
            [front.view.devices.show :as device.show]
            [front.view.devices.edit :as device.edit]
            [front.view.devices.raw-device-logs.index :as device.raw-device-log.index]
            [front.view.device-groups.index :as device-group.index]
            [front.view.device-groups.create :as device-group.create]
            [front.view.device-groups.show :as device-group.show]
            [front.view.device-groups.edit :as device-group.edit]
            [front.view.device-groups.raw-device-logs.index :as device-group.raw-device-log.index]
            [front.view.page404 :as page404]
            ["react-router-dom" :as router]))

(re-graph/init {:http {:url "/graphql"
                       :supported-operations #{:query :mutate}}
                :ws nil})

;https://reactrouter.com/en/main/routers/create-browser-router
(def app-router
  (router/createBrowserRouter
   (clj->js
    [{:path "/"
      :id "user-loggedin"
      :loader layout/loader
      :shouldRevalidate (fn [] true)
      :element (r/as-element [:f> layout/core])
      :children
      [{:index true :element (r/as-element [:f> log.index/core])}
       {:path "front"
        :children
        [{:index true :element (r/as-element [:f> dashboard/core])}
         {:path "login" :element (r/as-element [:f> login/core])}
         {:path "users"
          :children
          [{:index true :element (r/as-element [:f> user.index/core])}
           {:path "create" :element (r/as-element [:f> user.create/core])}
           {:path ":id_user" :element (r/as-element [:f> user.show/core])}
           {:path ":id_user/edit" :element (r/as-element [:f> user.edit/core])}]}
         {:path "devices"
          :children
          [{:index true :element (r/as-element [:f> device.index/core])}
           {:path "create" :element (r/as-element [:f> device.create/core])}
           {:path ":id_device"
            :children
            [{:index true :element (r/as-element [:f> device.show/core])}
             {:path "edit" :element (r/as-element [:f> device.edit/core])}
             {:path "raw_device_logs"
              :children
              [{:index true :element (r/as-element [:f> device.raw-device-log.index/core])}]}]}]}
         {:path "device_groups"
          :children
          [{:index true :element (r/as-element [:f> device-group.index/core])}
           {:path "create" :element (r/as-element [:f> device-group.create/core])}
           {:path ":id_device_group"
            :children
            [{:index true :element (r/as-element [:f> device-group.show/core])}
             {:path "edit" :element (r/as-element [:f> device-group.edit/core])}
             {:path "raw_device_logs" :element (r/as-element [:f> device-group.raw-device-log.index/core])}]}]}
         {:path "*" :element (r/as-element [:f> page404/core]) :status 404}]}]}])))

(defonce root (rc/create-root (.getElementById js/document "app")))

(defn init []
  (println "init app")
  (rc/render root [:> router/RouterProvider {:router app-router}]))

(defn ^:dev/after-load re-render []
  (init))
