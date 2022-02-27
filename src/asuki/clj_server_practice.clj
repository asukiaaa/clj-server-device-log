(ns asuki.clj-server-practice
  (:require [bidi.ring :as br]
            [clojure.java.jdbc :as jdbc]
            [hiccup.core :as hc]
            [ring.adapter.jetty :as server])
  (:gen-class))

(def db-spec (or (System/getenv "DATABASE_URL")
                 {:dbtype "mysql"
                  :host "mariadb"
                  :port 3306
                  :dbname "server_practice"
                  :user "maria-user"
                  :password "maria-pass"}))
;(println db-spec)

(defn init-db []
  ;; (jdbc/get-connection db-spec) ; check connection to db
  (let [table-info (jdbc/query db-spec "Show tables")
        tables (for [x table-info] (first (vals x)))]
    ;; (println table-info)
    ;; (println tables)
    (when-not (.contains tables "book")
      (println "required book table")
      (jdbc/db-do-commands db-spec (jdbc/create-table-ddl :book {:id :serial :name "varchar(100)"})))
    (when-not (.contains tables "user")
      (println "required user table")
      (jdbc/db-do-commands db-spec (jdbc/create-table-ddl :user {:id :serial :name "varchar(100)"}))))
  (let [users (jdbc/query db-spec "select * from user")]
    (println users)
    (when (= 0 (count users))
      (jdbc/insert-multi! db-spec :user (for [i (range 5)]
                                          {:name (str "hoi" i)})))))

(defn- top [req]
  {:status 200
   :body (hc/html
          [:div
           [:h1 "top page"]
           [:a {:href "/users"} "users"]])})

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

(defn- handle-404 [req]
  {:status 404})

(defonce server (atom nil))
(def route
  ["/"
   {"" top
    ;"graphql" graphql-handler
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
  (init-db)
  (start-server))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
