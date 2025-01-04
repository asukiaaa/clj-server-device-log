(ns back.config
  (:require [ragtime.jdbc :as rjdbc]))

(def db-spec (or (System/getenv "DATABASE_URL")
                 {:dbtype "mysql"
                  :host "mariadb"
                  :port 3306
                  :dbname "server_practice"
                  :user "maria-user"
                  :password "maria-pass"}))

(def key-auth (or (System/getenv "KEY_AUTHORIZATION")
                  "XXYYZZ"))

(def secret-for-session (or (System/getenv "SECRET_FOR_SESSION")
                            "secret-for-session-xxyyzz"))

(def port (if-let [str-port (System/getenv "PORT")]
            (read-string str-port)
            80))

(defn build-for-ragtime []
  {:datastore (rjdbc/sql-database db-spec)
   :migrations (rjdbc/load-resources "migrations")})

(def path-filestorage (or (System/getenv "PATH_FILESTORAGE") "../filestorage"))
