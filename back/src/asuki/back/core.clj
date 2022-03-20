(ns asuki.back.core
  (:require [bidi.ring :as br]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.reload :refer [wrap-reload]]
            [asuki.back.models.common :refer [init-db]]
            [asuki.back.route :as route])
  (:gen-class))

(defonce server (atom nil))

(def handler
  (br/make-handler route/main))

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

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (init-db)
  (let [port (if-let [str-port (System/getenv "PORT")]
               (read-string str-port)
               80)]
    (start-server port)))
