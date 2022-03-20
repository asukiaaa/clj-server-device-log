(ns asuki.back.config)

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
