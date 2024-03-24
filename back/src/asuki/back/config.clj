(ns asuki.back.config)

(def db-spec (or (System/getenv "DATABASE_URL")
                 {:dbtype "mysql"
                  :host "mariadb"
                  :port 3306
                  :dbname "server_practice"
                  :user "maria-user"
                  :password "maria-pass"}))

(def key-auth (or (System/getenv "KEY_AUTHORIZATION")
                  "XXYYZZ"))
