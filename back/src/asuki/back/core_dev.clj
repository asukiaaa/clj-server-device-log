(ns asuki.back.core-dev
  (:require [asuki.back.core :as core]
            [nrepl.server]
            [clojure.tools.namespace.repl :refer [refresh]]))

(defn run-dev-servers [& _args]
  (nrepl.server/start-server :port 59595)
  (core/run-relodable))

(defn db-migrate-with-reloading []
  #_(refresh)
  (core/db-migrate))

(defn db-rollback-with-reloading []
  #_(refresh)
  (core/db-rollback))
