(ns front.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rc]
            [re-graph.core :as re-graph]
            [front.view.top :as top]
            [front.view.login :as login]
            [front.view.dashboard :as dashboard]
            [front.view.layout :as layout]
            [front.view.users.index :as user.index]
            [front.view.users.create :as user.create]
            [front.view.users.show :as user.show]
            [front.view.users.edit :as user.edit]
            [front.view.users.password-reset :as user.password-reset]
            [front.view.user-teams.index :as user-team.index]
            [front.view.user-teams.create :as user-team.create]
            [front.view.user-teams.show :as user-team.show]
            [front.view.user-teams.edit :as user-team.edit]
            [front.view.devices.index :as device.index]
            [front.view.devices.create :as device.create]
            [front.view.devices.show :as device.show]
            [front.view.devices.edit :as device.edit]
            [front.view.devices.device-files.index :as device.device-files.index]
            [front.view.devices.device-logs.index :as device.device-log.index]
            [front.view.device-types.index :as device-type.index]
            [front.view.device-types.create :as device-type.create]
            [front.view.device-types.show :as device-type.show]
            [front.view.device-types.edit :as device-type.edit]
            [front.view.device-types.device-logs.index :as device-type.device-log.index]
            [front.view.device-types.device-type-api-keys.index :as device-type.device-type-api-key.index]
            [front.view.device-types.device-type-api-keys.create :as device-type.device-type-api-key.create]
            [front.view.device-types.device-type-api-keys.show :as device-type.device-type-api-key.show]
            [front.view.device-types.device-type-api-keys.edit :as device-type.device-type-api-key.edit]
            [front.view.watch-scopes.index :as watch-scope.index]
            [front.view.watch-scopes.create :as watch-scope.create]
            [front.view.watch-scopes.show :as watch-scope.show]
            [front.view.watch-scopes.edit :as watch-scope.edit]
            #_[front.view.watch-scopes.watch-scope-terms.index :as watch-scope.watch-scope-term.index]
            #_[front.view.watch-scopes.watch-scope-terms.create :as watch-scope.watch-scope-term.create]
            #_[front.view.watch-scopes.watch-scope-terms.show :as watch-scope.watch-scope-term.show]
            #_[front.view.watch-scopes.watch-scope-terms.edit :as watch-scope.watch-scope-term.edit]
            [front.view.watch-scopes.device-files.index :as watch-scope.device-file.index]
            [front.view.watch-scopes.device-logs.index :as watch-scope.device-log.index]
            [front.view.profile.index :as profile.index]
            [front.view.profile.password-edit :as profile.password-edit]
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
      :element (r/as-element [:f> layout/core]) :children
      [{:index true :element (r/as-element [:f> top/core])}
       {:path "front" :children
        [{:index true :element (r/as-element [:f> dashboard/core])}
         {:path "login" :element (r/as-element [:f> login/core])}
         {:path "profile" :children
          [{:index true :element (r/as-element [:f> profile.index/core])}
           {:path "password_edit" :element (r/as-element [:f> profile.password-edit/core])}]}
         {:path "users" :children
          [{:index true :element (r/as-element [:f> user.index/core])}
           {:path "create" :element (r/as-element [:f> user.create/core])}
           {:path ":user_id" :children
            [{:index true :element (r/as-element [:f> user.show/core])}
             {:path "edit" :element (r/as-element [:f> user.edit/core])}
             {:path "password_reset/:hash_password_reset" :element (r/as-element [:f> user.password-reset/core])}]}]}
         {:path "user_teams" :children
          [{:index true :element (r/as-element [:f> user-team.index/core])}
           {:path "create" :element (r/as-element [:f> user-team.create/core])}
           {:path ":user_team_id" :children
            [{:index true :element (r/as-element [:f> user-team.show/core])}
             {:path "edit" :element (r/as-element [:f> user-team.edit/core])}]}]}
         {:path "devices" :children
          [{:index true :element (r/as-element [:f> device.index/core])}
           {:path "create" :element (r/as-element [:f> device.create/core])}
           {:path ":device_id" :children
            [{:index true :element (r/as-element [:f> device.show/core])}
             {:path "edit" :element (r/as-element [:f> device.edit/core])}
             {:path "device_logs" :children
              [{:index true :element (r/as-element [:f> device.device-log.index/core])}]}
             {:path "device_files" :element (r/as-element [:f> device.device-files.index/core])}]}]}
         {:path "device_types" :children
          [{:index true :element (r/as-element [:f> device-type.index/core])}
           {:path "create" :element (r/as-element [:f> device-type.create/core])}
           {:path ":device_type_id" :children
            [{:index true :element (r/as-element [:f> device-type.show/core])}
             {:path "edit" :element (r/as-element [:f> device-type.edit/core])}
             {:path "device_logs" :element (r/as-element [:f> device-type.device-log.index/core])}
             {:path "device_type_api_keys" :children
              [{:index true :element (r/as-element [:f> device-type.device-type-api-key.index/core])}
               {:path "create" :element (r/as-element [:f> device-type.device-type-api-key.create/core])}
               {:path ":device_type_api_key_id" :children
                [{:index true :element (r/as-element [:f> device-type.device-type-api-key.show/core])}
                 {:path "edit" :element (r/as-element [:f> device-type.device-type-api-key.edit/core])}]}]}]}]}
         {:path "watch_scopes" :children
          [{:index true :element (r/as-element [:f> watch-scope.index/core])}
           {:path "create" :element (r/as-element [:f> watch-scope.create/core])}
           {:path ":watch_scope_id" :children
            [{:index true :element (r/as-element [:f> watch-scope.show/core])}
             {:path "edit" :element (r/as-element [:f> watch-scope.edit/core])}
             #_{:path "watch_scope_terms" :children
                [{:index true :element (r/as-element [:f> watch-scope.watch-scope-term.index/core])}
                 {:path "create" :element (r/as-element [:f> watch-scope.watch-scope-term.create/core])}
                 {:path ":watch_scope_term_id" :children
                  [{:index true :element (r/as-element [:f> watch-scope.watch-scope-term.show/core])}
                   {:path "edit" :element (r/as-element [:f> watch-scope.watch-scope-term.edit/core])}]}]}
             {:path "device_files" :element (r/as-element [:f> watch-scope.device-file.index/core])}
             {:path "device_logs" :element (r/as-element [:f> watch-scope.device-log.index/core])}]}]}
         {:path "*" :element (r/as-element [:f> page404/core]) :status 404}]}]}])))

(defonce root (rc/create-root (.getElementById js/document "app")))

(defn init []
  (println "init app")
  (rc/render root [:> router/RouterProvider {:router app-router}]))

(defn ^:dev/after-load re-render []
  (init))
