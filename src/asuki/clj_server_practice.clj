(ns asuki.clj-server-practice
  (:require [bidi.bidi :as bidi]
            [bidi.ring :as br]
            [hiccup.core :as hc]
            [ring.adapter.jetty :as server])
  (:gen-class))

(defn- top [req]
  {:status 200
   :body (hc/html
          [:div
           [:h1 "top page"]
           [:a {:href "/users"} "users"]])})

(defn- users [req]
  {:status 200
   :body "users index"})

(defn- user [req]
  {:status 200
   :body (str "user " (:id (:params req)))})

(defn- handle-404 [req]
  {:status 404})

(defonce server (atom nil))
(def route
  ["/"
   {"" top
    "users" users
    "favicon.ico" handle-404
    ["users/" [#"\d+" :id]] user}])

(def handler
  (br/make-handler route))

(defn start-server []
  (when-not @server
    (let [port 3000]
      (reset! server (server/run-jetty handler {:port port :join? false}))
      (println (str "server starts on port " port)))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn restart-server []
  (when @server
    (stop-server)
    (start-server)))

(defn greet
  "Callable entry point to the application."
  [data]
  (println data)
  (start-server))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
