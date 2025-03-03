(ns front.view.devices.util
  (:require [front.route :as route]
            [front.view.util.label :as util.label]))

(defn build-related-links [id-device]
  [[util.label/logs (route/device-device-logs id-device)]
   [util.label/files (route/device-device-files id-device)]])
