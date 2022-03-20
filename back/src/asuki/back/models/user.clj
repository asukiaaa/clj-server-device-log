(ns asuki.back.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [asuki.back.config :refer [db-spec]]))

(defn get-all []
  (jdbc/query db-spec "select * from user"))

(defn get-by-id [id]
  (first (jdbc/query db-spec (str "select * from user where id = " id))))
