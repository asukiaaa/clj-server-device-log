(ns asuki.back.route
  (:require [asuki.back.handlers.core :as handlers]
            [io.pedestal.http.body-params :refer [body-params]]
            [io.pedestal.http :refer [html-body]]
            [io.pedestal.http.route :as route :refer [url-for]]
            [clojure.java.io :as io]
            [jdbc-ring-session.core :refer [jdbc-store]]
            [ring.util.response :refer [redirect]]
            [hiccup.page :refer [html5]]
            [io.pedestal.http.ring-middlewares :as middlewares]
            [asuki.back.config :as config]
            [asuki.back.handlers.graphql :as handler-graphql]))

(def interceptor-session (middlewares/session {:store (jdbc-store config/db-spec)}))
(def interceptors-common [(body-params)
                          html-body
                          interceptor-session])

#_(def backend-session
    (buddy.backend/session))

(defn handler-page-check
  [request]
  {:status 200
   :body (html5
          ;; [:a {:href (url-for :home-page-with-interceptors)} "home"] [:br]
          ;; [:a {:href (url-for :home-page-without-interceptor)} "home-without-interceptor"] [:br]
          [:a {:href (url-for :inject-session)} "inject session"] [:br]
          ;; [:a {:href (url-for :inject-without-redirect)} "inject-without-redirect"] [:br]
          [:a {:href (url-for :clear-session)} "clear session"] [:br]
          ;; [:a {:href (url-for :clear-without-redirect)} "clear-withtout-redirect"] [:br]
          [:p "check page"]
          [:p "request"]
          [:p (str request)]
          [:p "cookies"]
          [:p (str (:cookies request))]
          [:p "session"]
          [:p (str (:session request))])})

(defn handler-redirect-with-injecting-data
  [_request]
  (-> (redirect (url-for :show-session))
      #_(assoc :cookies {:some {:value "data-in-cookie" :http-only true}})
      (assoc-in [:session :some] {:value "data-in-session"})))

(defn handler-redirect-with-clear [_request]
  (-> (redirect (url-for :show-session))
      #_(assoc :cookies {:some {:max-age 0}})
      (assoc :session nil)))

(defn build-file-handler [path-dir]
  (fn [request]
    (let [path (-> request :path-info)]
      {:status 200
       :body (io/input-stream (str path-dir path))})))

(def main
  #{["/" :get [html-body handlers/top] :route-name :top]
    ["/front" :get [html-body handlers/top] :route-name :front-dashboard]
    ["/front/" :get [html-body handlers/top] :route-name :front-dashboard-with-slash]
    ["/front/*" :get [html-body handlers/top] :route-name :front]
    ["/graph" :get [html-body handlers/top] :route-name :graph]
    #_["/device_logs" :get [html-body handlers/device-logs] :route-name :device-logs]
    #_["/show-session"
       :get (conj interceptors-common handler-page-check)
       :route-name :show-session]
    #_["/show-session-inject"
       :get (conj interceptors-common handler-redirect-with-injecting-data)
       :route-name :inject-session]
    #_["/show-session-clear"
       :get (conj interceptors-common handler-redirect-with-clear)
       :route-name :clear-session]
    #_["/device_logs/:id" :get [html-body handlers/device-log]
       :route-name :show-device-log
       :constraints {:id #"[0-9]+"}]
    ["/graphql" :post (into [] (concat [interceptor-session] handler-graphql/core)) :route-name :graphql]
    ["/css/*" :get (build-file-handler "../front/resources/public") :route-name :handle-css]
    ["/out-cljs/*" :get (build-file-handler "../front/out-cljs/public") :route-name :handle-out-cljs]
    ["/api/raw_device_log"
     :post [(body-params) handlers/api-post-raw-device-log]
     :route-name :api-post-raw-device-log]
    ["/api/device"
     :post [(body-params) handlers/api-post-device]
     :route-name :api-post-device]
    ["/api/device_file"
     :post [(middlewares/multipart-params) handlers/api-post-device-file]
     :route-name :api-post-device-file]
    ["/filestorage/*"
     :get [interceptor-session (body-params) handlers/get-file-from-filestorage]
     :route-name :get-file-fromfilestorage]
    ["/404" :get [html-body handlers/handle-404] :route-name :show-404]})
