(ns front.view.top
  (:require [front.model.raw-device-log :as model.log]
            [front.view.util.raw-device-log.page :as raw-device-log.page]))

(defn core []
  (raw-device-log.page/core model.log/fetch-list-and-total))
