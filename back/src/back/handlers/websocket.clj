(ns back.handlers.websocket
  (:require [cheshire.core :as cheshire]
            [io.pedestal.interceptor :refer [interceptor]]
            [io.pedestal.service.websocket :as websocket]
            [java-time.api]
            [back.models.device :as model.device]
            [back.models.util :as model.util]
            [back.handlers.util :as handler.util]
            [back.models.device-type-api-key :as model.device-type-api-key]))

; https://github.com/pedestal/pedestal/blob/master/samples/jetty-web-sockets/src/jetty_web_sockets/service.clj
; https://github.com/pedestal/pedestal/blob/cccff06f3dd3adb285e5105873bfb356399aae0c/tests/test/io/pedestal/service/jetty_websocket_test.clj#L78
; https://github.com/pedestal/pedestal/blob/master/tests/test/io/pedestal/service/jetty_websocket_test.clj
; async-events is only for test
; https://github.com/pedestal/pedestal/blob/cccff06f3dd3adb285e5105873bfb356399aae0c/tests/test/io/pedestal/async_events.clj#L12

(def map-device (atom {}))
(def map-controller-device (atom {}))

(def ^:private default-ws-opts
  {:on-open (fn on-open [_channel request]
              (println :opened)
              ; (println request) ; stop and don't start on-text
              (println (keys request))
              (println (:headers request))
              (let [key-data (model.util/build-random-str-alphabets-and-number 10)]
                (println :map-device @map-device)
                (println :key-data key-data)
                {:key-data key-data}))
   :on-close (fn on-close [_channel proc _reason]
               (println :closed)
               (let [{:keys [key-data]} proc]
                 (println key-data)
                 #_(swap! map-device dissoc key-data)))
   :on-text (fn on-text [_channel _proc _text]
              #_(write-event :text text))
   :on-binary (fn on-binary [_channel _proc _buffer]
                #_(write-event :binary buffer))})

(def device-io
  (interceptor
   {:name ::device-io
    :enter
    (fn [context]
      (websocket/upgrade-request-to-websocket
       context
       (assoc default-ws-opts
              :on-open
              (fn on-open [channel request]
                (println (:headers request))
                (let [str-bearer (handler.util/get-bearer request)
                      device (model.device/get-by-authorization-bearer str-bearer)
                      id-device (:id device)
                      id-device-type (:device_type_id device)]
                  (swap! map-device assoc-in [id-device-type id-device] channel)
                  {model.device/key-table device}))
              :on-close
              (fn on-close [_channel proc _reason]
                (println :closed)
                (let [device (model.device/key-table proc)
                      id-device (:id device)
                      id-device-type (:device_type_id device)]
                  (println model.device/key-table device)
                  (swap! map-device update-in [id-device-type] dissoc id-device)))
              :on-text
              (fn [_channel proc text]
                (let [device (model.device/key-table proc)
                      id-device (:id device)]
                  (when-let [channels (-> @map-controller-device (get id-device))]
                    (doseq [channel-controller (vals channels)]
                      (websocket/send-text! channel-controller text))))))))}))

(def device-control
  (interceptor
   {:name ::device-control
    :enter
    (fn [context]
      (let [id-device (get-in context [:request :path-params :id_device])
            id-device (when id-device (Integer/parseInt id-device))]
        (websocket/upgrade-request-to-websocket
         context
         (assoc default-ws-opts
                :on-open
                (fn on-open [channel request]
                  (println (:headers request))
                  (let [str-bearer (handler.util/get-bearer request)
                        device-type-api-key (model.device-type-api-key/get-by-authorization-bearer str-bearer)
                        has-permission-control-device (-> device-type-api-key :permission (cheshire/parse-string true) :control_device)]
                    (when-not has-permission-control-device
                      (throw (Exception. "Does not have permission to control_device")))
                    (swap! map-controller-device assoc-in [id-device (:id device-type-api-key)] channel)
                    {model.device-type-api-key/key-table device-type-api-key}))
                :on-close
                (fn on-close [_channel proc _reason]
                  (println :closed)
                  (let [device-type-api-key (model.device-type-api-key/key-table proc)]
                    (println model.device-type-api-key/key-table device-type-api-key)
                    (swap! map-controller-device update-in [id-device] dissoc (:id device-type-api-key))))
                :on-text
                (fn [_channel proc text]
                  (let [device-type-api-key (model.device-type-api-key/key-table proc)]
                    (when-let [channel-device (-> @map-device
                                                  (get (:device_type_id device-type-api-key))
                                                  (get id-device))]
                      (websocket/send-text! channel-device text))))))))}))

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
                                  (websocket/send-text! channel (str prefix " " text)))))))}))
