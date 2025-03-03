(ns back.models.user-team-device-config
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [back.config :refer [db-spec]]
            [back.models.util.user-team-device-config :as util.user-team-device-config]
            [back.models.util :as model.util]))

(def name-table util.user-team-device-config/name-table)
(def key-table util.user-team-device-config/key-table)

(defn filter-params [params]
  (select-keys params [:user_team_id :device_id :config]))

(defn delete-and-create-for-device [params id-device & [{:keys [transaction]}]]
  (jdbc/with-db-transaction [transaction (or transaction db-spec)]
    (jdbc/delete! (or transaction db-spec) key-table ["device_id = ?" id-device])

    (when (seq params)
      (model.util/create key-table (assoc params :device_id id-device) {:transaction transaction}))))
