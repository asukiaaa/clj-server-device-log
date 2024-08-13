(ns asuki.back.models.device-group
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :refer [join escape]]
            [clojure.core :refer [re-find re-matcher]]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [asuki.back.config :refer [db-spec]]
            [asuki.back.models.util :as model.util]))

(defn delete [id]
  (jdbc/delete! db-spec :device_group ["id = ?" id]))

(defn update [id params]
  (jdbc/update! db-spec :device_group params ["id = ?" id]))

(defn create [params]
  (jdbc/insert! db-spec :device_group params))

(defn get-list-with-total [params]
  (-> (model.util/build-query-get-index "device_group")
      (model.util/append-limit-offset-by-limit-page-params params)
      model.util/get-list-with-total))
