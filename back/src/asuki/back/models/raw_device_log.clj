(ns asuki.back.models.raw-device-log
  (:require [clojure.java.jdbc :as jdbc]
            [asuki.back.config :refer [db-spec]]))

(defn get-all []
  (jdbc/query db-spec "select * from raw_device_log ORDER BY created_at DESC"))

(defn get-by-id [id]
  (first (jdbc/query db-spec (str "select * from raw_device_log where id = " id))))

(defn create [params]
  (jdbc/insert! db-spec :raw_device_log params))
