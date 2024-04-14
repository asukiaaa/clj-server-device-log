(ns asuki.back.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as cstr]
            [buddy.core.hash :as bhash]
            [buddy.core.codecs :as codecs]
            [asuki.back.config :refer [db-spec]]))

(defn build-hash [password salt]
  (-> (str password salt) bhash/sha3-512 codecs/bytes->hex))

(defn random-str [len]
  (apply str (repeatedly len #(rand-nth "abcdefghijklmnopqrstuvwxyz0123456789:[]\\/.,\"!#$%&'()-^"))))

(defn get-all []
  (jdbc/query db-spec "SELECT * FROM user"))

(defn get-by-id [id]
  (first (jdbc/query db-spec
                     #_(str "SELECT * FROM user WHERE id = " id)
                     ["SELECT * FROM user WHERE id = ?" id])))

(defn get-by-email [email & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec) ["SELECT * FROM user WHERE email = ?" email])))

(defn create-with-password [params]
  (let [email (:email params)
        password (:password params)
        name (:name params)
        salt (random-str 20)
        hash (build-hash password salt)]
    ; todo check exists of name, email and password
    ; todo check email format
    ; todo check password len more than 10
    (println salt hash)
    (jdbc/with-db-transaction [t-con db-spec]
      (let [user (get-by-email email {:transaction t-con})]
        (if (empty? user)
          (jdbc/insert! t-con :user
                        (-> params
                            (dissoc :password)
                            (assoc :salt salt)
                            (assoc :hash hash)))
          {:error "User already exists."})))))

(defn get-by-email-password [email password]
  (when-let [user (get-by-email email)]
    (when (= (:hash user) (build-hash password (:salt user)))
      user)))

(defn get-total-count []
  (-> (jdbc/query db-spec "SELECT COUNT(*) FROM user;") first vals first))

(defn create-sample-admin-if-no-user []
  (when (= (get-total-count) 0)
    (create-with-password {:email "admin@example.com"
                           :name "admin"
                           :password "admin-pass"})))

(defn filter-for-session [user]
  (select-keys user [:id :name :email :created_at]))
