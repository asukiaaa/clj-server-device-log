(ns back.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [ns-tracker.core :refer [ns-tracker]]
            [ragtime.repl :as ragr]
            [back.models.user :as user]
            [back.models.device-file :as model.device-file]
            [back.config :as config]
            [back.route :refer [main] :rename {main routes-main}]))

(def modified-namespaces (ns-tracker "src"))

(def routes-target routes-main)

(defn watched-routes-target []
  (doseq [ns-sym (modified-namespaces)]
    (require ns-sym :reload))
  (route/expand-routes routes-target))

(defn start-server [port & args]
  (let [host "0.0.0.0"
        relodable (when-not (nil? args) (.contains args :relodable))]
    (-> {::http/routes routes-target
         ::http/port port
         ::http/host host
         ::http/resource-path "public"
         ::http/type :jetty
         ::http/secure-headers {:content-security-policy-settings {:object-src "'none'"}}}
        (merge (if relodable
                 {::http/routes watched-routes-target
                  ::http/join? false}
                 nil))
        http/create-server
        http/start)
    (println (str "server starts on http://" host ":" port))))

(defn run-relodable [& _args]
  (start-server 3000 :relodable))

(defn db-migrate []
  (ragr/migrate (config/build-for-ragtime))
  (user/create-sample-admin-if-no-user))

(defn db-rollback []
  (ragr/rollback (config/build-for-ragtime)))

(defn -main [& args]
  (condp = (first args)
    "server-with-migration"
    (do
      (db-migrate)
      (start-server config/port))
    "server" (start-server config/port)
    "load-local-device-files" (model.device-file/update-for-files-on-local)
    "db" (condp = (second args)
           "migrate" (db-migrate)
           "rollback" (db-rollback))
    (println (str "no matching key" (first args)))))
