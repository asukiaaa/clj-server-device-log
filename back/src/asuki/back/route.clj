(ns asuki.back.route
  (:require [asuki.back.handlers.core :as handlers]
            [bidi.ring :as br]
            [ring.middleware.json :refer [wrap-json-body]]))

(def main
  ["/"
   {"" handlers/top
    "cljs-out" (br/->Files {:dir "../front/target/public/cljs-out"})
    "front/out-webpack" (br/->Files {:dir "../front/out-webpack"})
    ;; "graphql" graphql-handler
    "device_logs" {"" handlers/device-logs
                   ["/" [#"\d+" :id]] handlers/device-log}
    "favicon.ico" handlers/handle-404
    "users" {"" handlers/users
             ["/" [#"\d+" :id]] handlers/user}
    "api"
    {"/raw_device_log" (br/->WrapMiddleware handlers/api-raw-device-log wrap-json-body)}
    [:*] handlers/handle-404}])
