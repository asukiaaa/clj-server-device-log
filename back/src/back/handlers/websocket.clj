(ns back.handlers.websocket
  (:require [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.service.websocket :as websocket]))

; https://github.com/pedestal/pedestal/blob/master/samples/jetty-web-sockets/src/jetty_web_sockets/service.clj
; https://github.com/pedestal/pedestal/blob/cccff06f3dd3adb285e5105873bfb356399aae0c/tests/test/io/pedestal/service/jetty_websocket_test.clj#L78
; https://github.com/pedestal/pedestal/blob/master/tests/test/io/pedestal/service/jetty_websocket_test.clj
; async-events is only for test
; https://github.com/pedestal/pedestal/blob/cccff06f3dd3adb285e5105873bfb356399aae0c/tests/test/io/pedestal/async_events.clj#L12

(def ^:private default-ws-opts
  {:on-open (fn on-open [channel request]
              (println :opened)
              ; (println request) ; stop and don't start on-text
              (println (keys request))
              (println (:headers request)))
   :on-close (fn on-close [_channel _proc reason]
               (println :closed)
               #_(write-event :close reason))
   :on-text (fn on-text [_channel _proc text]
              #_(write-event :text text))
   :on-binary (fn on-binary [_channel _proc buffer]
                #_(write-event :binary buffer))})

(def echo-prefix
  (interceptor
   {:name  ::echo-prefix
    :enter (fn [context]
             (let [prefix (get-in context [:request :path-params :prefix])]
               (websocket/upgrade-request-to-websocket
                context
                (assoc default-ws-opts
                       :on-text (fn [channel proc text]
                                  (println :received text)
                                  (println :proc proc)
                                  #_(write-event :server-text text)
                                  (websocket/send-text! channel (str prefix " " text)))))))}))
