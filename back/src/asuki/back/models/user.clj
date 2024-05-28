(ns asuki.back.models.user
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [buddy.core.hash :as bhash]
            [buddy.core.codecs :as codecs]
            [asuki.back.config :refer [db-spec]]
            [asuki.back.models.util :as model.util]))

(defn build-hash [password salt]
  (-> (str password salt) bhash/sha3-512 codecs/bytes->hex))

(defn random-str [len]
  (apply str (repeatedly len #(rand-nth "abcdefghijklmnopqrstuvwxyz0123456789:[]\\/.,\"!#$%&'()-^"))))

(defn get-by-email [email & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec) ["SELECT * FROM user WHERE email = ?" email])))

(defn get-by-id [id & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec) ["SELECT * FROM user WHERE id = ?" id])))

(defn filter-params-to-create [params]
  (select-keys params [:email :name :permission]))

(defn validate-password [password]
  (cond-> nil
    (nil? password) (conj "required")
    (< (count password) 10) (conj "needed 10 chars or more")))

(defn validate-email [email]
  (cond-> nil
    (nil? email) (conj "required")
    (not (re-matches #".+\@.+\..+" email)) (conj "invalid format")))

(defn validate-permission [permission]
  (if (or (empty? permission) (= permission "null"))
    ["required"]
    (let [parsed (model.util/parse-json permission)]
      (when-let [error (:error parsed)] [error]))))

(defn validate-params-exept-for-password [errors params]
  (reduce (fn [errors [key validate]]
            (if-let [error (validate (get params key))]
              (assoc errors key error)
              errors))
          errors {:email validate-email
                  :permission validate-permission}))

(defn validate-params-for-password [errors params]
  (if-let [errors-for-password (validate-password (:password params))]
    (assoc errors :password (concat (:password errors) errors-for-password))
    errors))

(defn create-with-password [params]
  (println :creae-with-password params)
  (jdbc/with-db-transaction [t-con db-spec]
    (let [email (:email params)
          user-in-db (get-by-email email {:transaction t-con})]
      (if-let [errors (cond-> (-> nil
                                  (validate-params-exept-for-password params)
                                  (validate-params-for-password params))
                        (seq user-in-db) (assoc :email ["User already exists"]))]
        {:errors (json/write-str errors)}
        (let [password (:password params)
              salt (random-str 20)
              hash (build-hash password salt)]
          ; (println salt hash)
          (jdbc/insert! t-con :user
                        (-> params
                            filter-params-to-create
                            (dissoc :password)
                            (assoc :salt salt)
                            (assoc :hash hash)))
          {:user (get-by-email email {:transaction t-con})})))))

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
                           :password "admin-pass"
                           :permission "{\"role\": \"admin\"}"})))

(defn get-list-with-total [args]
  (let [str-query "SELECT * FROM user;"]
    (model.util/get-list-with-total str-query)))

(defn filter-for-session [user]
  (select-keys user [:id]))
