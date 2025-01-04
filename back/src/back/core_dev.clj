(ns back.core-dev
  (:require [back.core :as core]
            [nrepl.server]))

(defn run-dev-servers [& _args]
  (nrepl.server/start-server :port 59595
                             :bind "0.0.0.0")
  (core/run-relodable))
