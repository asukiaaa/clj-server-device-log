(ns asuki.back.config
  (:require [ragtime.jdbc :as rjdbc]))

(def db-spec (or (System/getenv "DATABASE_URL")
                 (System/getenv "JAWSDB_MARIA_URL")
                 {:dbtype "mysql"
                  :host "mariadb"
                  :port 3306
                  :dbname "server_practice"
                  :user "maria-user"
                  :password "maria-pass"}))

(def key-auth (or (System/getenv "KEY_AUTHORIZATION")
                  "XXYYZZ"))

(def port (if-let [str-port (System/getenv "PORT")]
            (read-string str-port)
            80))

(def ragtime
  {:datastore (rjdbc/sql-database db-spec)
   :migrations (rjdbc/load-resources "migrations")})
