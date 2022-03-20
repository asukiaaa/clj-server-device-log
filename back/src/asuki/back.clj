(ns asuki.back
  (:require [bidi.ring :as br]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.java.jdbc :as jdbc]
            [hiccup.page :refer [html5]]
            [hiccup.core :as hc]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.reload :refer [wrap-reload]]
            #_[ring.middleware.x-headers :refer [wrap-frame-options]])
  (:gen-class))

(def db-spec (or (System/getenv "DATABASE_URL")
                 (System/getenv "JAWSDB_MARIA_URL")
                 {:dbtype "mysql"
                  :host "mariadb"
                  :port 3306
                  :dbname "server_practice"
                  :user "maria-user"
                  :password "maria-pass"}))
;(println db-spec)
(def key-auth (or (System/getenv "KEY_AUTHORIZATION")
                  "XXYYZZ"))

(defn init-db []
  ;; (jdbc/get-connection db-spec) ; check connection to db
  (let [table-info (jdbc/query db-spec "Show tables")
        tables (for [x table-info] (first (vals x)))]
    ;; (println table-info)
    ;; (println tables)
    (when-not (.contains tables "book")
      (println "required book table")
      (->> (jdbc/create-table-ddl
            :book {:id :serial
                   :name "varchar(100)"})
           (jdbc/db-do-commands db-spec)))
    (when-not (.contains tables "user")
      (println "required user table")
      (->> (jdbc/create-table-ddl
            :user {:id :serial
                   :name "varchar(100)"})
           (jdbc/db-do-commands db-spec)))
    (when-not (.contains tables "raw_device_log")
      (println "required raw_device_log table")
      (->> (jdbc/create-table-ddl
            :raw_device_log {:id :serial
                             :data "JSON NOT NULL"
                             :created_at "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"})
           (jdbc/db-do-commands db-spec)))))

(defn create-test-data []
  (let [users (jdbc/query db-spec "select * from user")]
    (println users)
    (when (= 0 (count users))
      (jdbc/insert-multi! db-spec :user (for [i (range 5)]
                                          {:name (str "hoi" i)})))))

(defn- top [req]
  {:status 200
   ;:headers {"Content-Type" "text/html"}
   :body (html5
          [:head
           #_[:script {:src "./out/main.js" :type "text/javascript"}]
           #_[:base {:href "/"}]
           #_[:script {:src "/front/out/main.js" :type "module"}]]
          [:body
           [:div {:id "app"} "maybe loading js"]
           [:script {:src "/front/out-webpack/main.js"
                     :type "text/javascript"}]
           #_[:script {:src "/front/out/index.js"
                       :type "module"}]
           #_[:base {:href "/front/target/public/"}]
           #_[:script {:src "cljs-out/figwheel-dev-main.js"
                       :type "text/javascript"}]
           #_[:script {:src "./out-webpack/main.js"
                       :type "text/javascript"}]
           [:div
            [:h1 "top page"]
            [:p [:a {:href "/users"} "users"]]
            [:p [:a {:href "/device_logs"} "device logs"]]]])})

(defn- users [req]
  (let [users (jdbc/query db-spec "select * from user")]
    {:status 200
     :body (hc/html
            [:div
             [:h1 "users"]
             [:ul
              (for [user users]
                [:li
                 [:a {:href (str "/users/" (:id user))}
                  (str (:id user) " " (:name user))]])]])}))

(defn- user [req]
  (let [id (:id (:params req))
        user (first (jdbc/query db-spec (str "select * from user where id = " id)))]
    {:status 200
     :body (hc/html
            (if user
              [:div
               [:p (:id user)]
               [:p (:name user)]]
              [:div "no user"]))}))

(defn- device-logs [req]
  (let [logs (jdbc/query db-spec "select * from raw_device_log ORDER BY created_at DESC")]
    {:status 200
     :body (hc/html
            [:div
             [:h1 "device logs"]
             [:table
              [:thead [:tr [:th "id"] [:th "created_at"] [:th "action"]]]
              [:tbody
               (for [log logs]
                 [:tr
                  [:td (:id log)]
                  [:td (:created_at log)]
                  [:td [:a {:href (str "/device_logs/" (:id log))}
                        "detail"]]])]]])}))

(defn- device-log [req]
  (let [id (:id (:params req))
        log (first (jdbc/query db-spec (str "select * from raw_device_log where id = " id)))]
    {:status 200
     :body (hc/html
            (if log
              [:div
               [:p (:id log)]
               [:p [:pre {:style "overflow:auto"}
                    (-> (:data log) json/read-json json/pprint-json with-out-str)]]
               [:p (:created_at log)]]
              [:div "no data"]))}))

(defn- handle-404 [req]
  {:status 404
   :body (hc/html
          [:div "404 not found"])})

(defn- api-raw-device-log [req]
  (let [request-method (:request-method req)]
    (println req)
    (when (and (= request-method :post)
               (-> (:headers req)
                   (get "authorization")
                   (str/split #" ")
                   #_((fn [x] (println x) x))
                   (last)
                   (= key-auth)))
      (let [body (:body req)]
        (println body)
        (jdbc/insert! db-spec :raw_device_log {:data (json/write-str body)}))
      {:status 200
       :body "ok"})))

(defonce server (atom nil))
(def route
  ["/"
   {"" top
    "cljs-out" (br/->Files {:dir "../front/target/public/cljs-out"})
    "front/" {"" (br/->Files {:dir "../front"})}
    ;; "graphql" graphql-handler
    "device_logs" {"" device-logs
                   ["/" [#"\d+" :id]] device-log}
    "favicon.ico" handle-404
    "users" users
    ["users/" [#"\d+" :id]] user
    "api"
    {"/raw_device_log" (br/->WrapMiddleware api-raw-device-log wrap-json-body)}
    [:*] handle-404}])

(def handler
  (br/make-handler route))

(def relodable-handler
  (wrap-reload #'handler))

(defn start-server [& args]
  (let [port (.get args 0)
        host "0.0.0.0"
        relodable (.contains args :relodable)]
    ;; (println args " port " port " relodable " relodable)
    (when-not @server
      (reset! server (run-jetty
                      (if relodable
                        relodable-handler
                        handler)
                      {:port port :host host :join? false}))
      (println (str "server starts on http://" host ":" port)))))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))

(defn restart-server []
  (when @server
    (stop-server)
    (start-server)))

(defn run-relodable
  "Callable entry point to the application."
  [& args]
  (init-db)
  (start-server 3000 :relodable))

(defn greet
  "Callable entry point to the application."
  [data]
  (println data)
  (init-db)
  (start-server))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (init-db)
  (let [port (if-let [str-port (System/getenv "PORT")]
               (read-string str-port)
               80)]
    (start-server port)))
