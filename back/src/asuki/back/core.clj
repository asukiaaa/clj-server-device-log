(ns asuki.back.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            #_[ring.adapter.jetty :refer [run-jetty]]
            #_[ring.middleware.reload :refer [wrap-reload]]
            #_[io.pedestal.http.route.definition :refer [defroutes]]
            [asuki.back.models.common :refer [init-db]]
            [asuki.back.route :as route]))

(defn start-server [& args]
  (let [port (.get args 0)
        host "0.0.0.0"
        relodable (.contains args :relodable)]
    ;; TODO hande relodable
    (println (str "in start-server " host ":" port))
    (-> {::http/routes route/main
         ::http/port port
         ::http/host host
         ::http/resource-path "public"
         ::http/type :jetty
         ::http/secure-headers {:content-security-policy-settings {:object-src "'none'"}}}
        http/create-server
        http/start)
    (println (str "server starts on http://" host ":" port))))

(defn run-relodable
  [& args]
  (init-db)
  (start-server 3000 :relodable))

(defn -main
  [& args]
  (init-db)
  (let [port (if-let [str-port (System/getenv "PORT")]
               (read-string str-port)
               80)]
    (start-server port)))
