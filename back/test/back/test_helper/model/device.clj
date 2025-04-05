(ns back.test-helper.model.device
  (:require
   [clojure.core :refer [format]]
   [back.models.device :as model.device]))

(defmacro with-devices
  {:clj-kondo/lint-as 'clojure.core/let}
  [[devices [device-type user-team & [options]]] & body]
  `(let [~devices (for [i# (range (or (:number-devices ~options) 2))]
                    (model.device/create {:name (format "device %d for %s" i# (:name ~user-team))
                                          :user_team_id (:id ~user-team)
                                          :device_type_id (:id ~device-type)}))]
     (try
       ~@body
       (finally
         (doseq [device# ~devices]
           (model.device/delete (:id device#)))))))
