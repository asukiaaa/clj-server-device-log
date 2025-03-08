(ns front.view.device-types.util
  (:require [front.route :as route]
            [front.view.util.label :as util.label]))

(defn build-related-links [id-device-type]
  [[util.label/api-keys (route/device-type-device-type-api-keys id-device-type)]
   [util.label/logs (route/device-type-device-logs id-device-type)]
   [util.label/user-team-configs (route/device-type-user-team-configs id-device-type)]])
