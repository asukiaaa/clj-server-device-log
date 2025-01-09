(ns front.view.top
  (:require [front.model.raw-device-log :as model.log]
            [front.view.util.raw-device-log.page :as raw-device-log.page]))

(def map-default
  {:str-renderer "[{\"key\": \"id\"}, {\"key\": [\"data\", \"camera_id\"], \"badge\": [{\"text\": \"not wakeup\", \"when\": {\"key\": \"created_at\", \"action\": \"not-in-hours-24\"}}]}, {\"label\": \"battery\", \"key\": [\"data\", \"readonly_state\", \"volt_battery\"]}, {\"label\": \"panel\", \"key\": [\"data\", \"readonly_state\", \"volt_panel\"]}, {\"key\": \"created_at\"}]"
   :str-where "[{\"key\": \"created_at\", \"action\": \"in-hours-24\"}]"
   :str-order "[{\"key\": [\"data\", \"camera_id\"], \"dir\": \"desc\"},{\"key\":\"created_at\",\"dir\":\"desc\"}]"})

(defn core []
  (raw-device-log.page/core model.log/fetch-list-and-total {:map-default map-default}))
