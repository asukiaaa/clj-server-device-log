(ns back.models.user
  (:refer-clojure :exclude [update])
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.data.json :as json]
            [clojure.walk :refer [keywordize-keys]]
            [buddy.core.hash :as bhash]
            [buddy.core.codecs :as codecs]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [back.config :refer [db-spec]]
            [back.models.util :as model.util]
            [back.models.util.user :as util.user]
            [back.util.label :as util.label]))

(def name-table util.user/name-table)
(def key-table util.user/key-table)

(defn delete [id]
  ; TODO prohibit deleting when who has device-type
  (jdbc/delete! db-spec :user ["id = ?" id]))

(defn admin? [user]
  (when-let [str-permission (:permission user)]
    (-> str-permission json/read-json :role (= "admin"))))

(defn build-hash [password salt]
  (-> (str password salt) bhash/sha3-512 codecs/bytes->hex))


(defn- get-by-email-bare [email & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     [(format "SELECT * FROM user WHERE email = ?")
                      (model.util/escape-for-sql email)])))

(defn get-by-email [email & [{:keys [transaction]}]]
  (first (jdbc/query (or transaction db-spec)
                     [(format "SELECT %s FROM user WHERE email = ?"
                              (util.user/build-str-keys-select-for-table))
                      (model.util/escape-for-sql email)])))

(defn get-by-id-with-permission [id & [{:keys [transaction]}]]
  (model.util/get-by-id
   id name-table
   {:str-keys-select (util.user/build-str-keys-select-for-table {:with-permission true})
    :transaction transaction}))

(defn get-by-id [id & [{:keys [transaction]}]]
  (-> (model.util/get-by-id
       id name-table
       {:str-keys-select (util.user/build-str-keys-select-for-table)
        :transaction transaction})))

(defn get-by-id-in-ids [id sql-ids & [{:keys [transaction]}]]
  (-> (model.util/get-by-id
       id name-table
       {:str-keys-select (util.user/build-str-keys-select-for-table)
        :str-where (format "%s.id IN %s" name-table sql-ids)
        :transaction transaction})))

(defn- get-by-id-bare [id & [{:keys [transaction]}]]
  (model.util/get-by-id id name-table {:transaction transaction}))

(defn get-by-id-and-hash-password-reset [id hash-password-reset & [{:keys [transaction]}]]
  (let [user (get-by-id-bare id {:transaction transaction})
        info-password-reset (when-let [str-password-reset (:password_reset user)]
                              (-> str-password-reset model.util/parse-json :parsed keywordize-keys))
        time-until (when-let [str-until (:until info-password-reset)]
                     (f/parse model.util/time-format-yyyymmdd-hhmmss str-until))]
    (when (and (= hash-password-reset (:hash info-password-reset))
               (not (nil? time-until))
               (t/after? time-until (t/now)))
      user)))

(defn filter-params-to-create [params]
  (select-keys params [:email :name :permission]))

(defn filter-params-to-update [params]
  (select-keys params [:email :name :permission]))

(defn validate-name [email]
  (cond-> nil
    (nil? email) (conj "required")))

(defn validate-password [password]
  (cond-> nil
    (nil? password) (conj "required")
    (< (count password) 10) (conj "needed 10 chars or more")))

(defn validate-email [email]
  (cond-> nil
    (nil? email) (conj "required")
    (not (re-matches #".+\@.+\..+" email)) (conj "invalid format")))

(defn validate-permission [permission]
  (when-not (empty? permission)
    (let [parsed (model.util/parse-json permission)]
      (when-let [error (:error parsed)] [error]))))

(defn check-errors-of-params-exept-for-password [errors params]
  (reduce (fn [errors [key validate]]
            (if-let [error (validate (get params key))]
              (assoc errors key error)
              errors))
          errors {:name validate-name
                  :email validate-email
                  :permission validate-permission}))

(defn append-error [errors key message]
  (assoc errors key (concat (get errors key) [message])))

(defn append-system-error [errors message]
  (let [key :__system]
    (append-error errors key message)))

(defn check-errors-of-params-for-password [errors params & [{:keys [key-password]}]]
  (let [key-password (or key-password :password)]
    (if-let [errors-for-password (validate-password (key-password params))]
      (assoc errors key-password (concat (key-password errors) errors-for-password))
      errors)))

(defn create-with-password [params]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [email (:email params)
          user-in-db (get-by-email email {:transaction t-con})]
      (if-let [errors (cond-> (-> nil
                                  (check-errors-of-params-exept-for-password params)
                                  (check-errors-of-params-for-password params))
                        (seq user-in-db) (append-error :email "User already exists"))]
        {:errors (json/write-str errors)}
        (let [password (:password params)
              salt (model.util/build-random-str-complex 20)
              hash (build-hash password salt)]
          ; (println salt hash)
          (jdbc/insert! t-con :user
                        (-> params
                            filter-params-to-create
                            (dissoc :password)
                            (assoc :salt salt)
                            (assoc :hash hash)))
          {:user (get-by-email email {:transaction t-con})})))))

(defn update [id params]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [user-in-db (get-by-id id {:transaction t-con})]
      (if-let [errors (cond-> (-> nil
                                  (check-errors-of-params-exept-for-password params))
                        (empty? user-in-db) (append-system-error "User does not exist"))]
        {:errors (json/write-str errors)}
        (do
          (let [params (filter-params-to-update params)]
            (jdbc/update! t-con :user
                          params
                          ["id = ?" id]))
          {:user (get-by-id id {:transaction t-con})})))))

(defn is-correct-password [user password]
  (= (:hash user) (build-hash password (:salt user))))

(defn get-by-email-password-with-permission [email password]
  (when-let [user (get-by-email-bare email)]
    (when (is-correct-password user password)
      (select-keys user util.user/keys-param-with-permission))))

(defn get-total-count []
  (-> (jdbc/query db-spec "SELECT COUNT(*) FROM user;") first vals first))

(defn create-sample-admin-if-no-user []
  (when (= (get-total-count) 0)
    (create-with-password {:email "admin@example.com"
                           :name "admin"
                           :password "admin-pass"
                           :permission "{\"role\": \"admin\"}"})))

(defn get-list-with-total-for-admin [args & [{:keys [str-where transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table args
   {:str-where str-where
    :str-keys-select (util.user/build-str-keys-select-for-table {:with-permission true})
    :transaction transaction}))

(defn get-list-with-total-by-ids [args sql-ids & [{:keys [transaction]}]]
  (model.util/get-list-with-total-with-building-query
   name-table args
   {:str-where (format "%s.id IN %s" name-table sql-ids)
    :str-keys-select (util.user/build-str-keys-select-for-table)
    :transaction transaction}))

(defn filter-for-session [user]
  (select-keys user [:id]))

(defn check-errors-is-correct-password [errors user password]
  (let [key-password :password]
    (if-not (is-correct-password user password)
      (assoc errors key-password (concat (key-password errors) ["Invalid password"]))
      errors)))

(defn reset-password-with-checking-current-password [id args]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [password-current (:password args)
          key-password-new :password_new
          password-new (key-password-new args)
          user (get-by-id id {:transaction t-con})]
      (if-let [errors (cond-> (-> nil
                                  (check-errors-is-correct-password user password-current)
                                  (check-errors-of-params-for-password args {:key-password key-password-new}))
                        (empty? user) (append-system-error "User does not exist"))]
        {:errors (json/write-str errors)}
        (do
          (jdbc/update! t-con :user
                        {:hash (build-hash password-new (:salt user))}
                        ["id = ?" id])
          {:message "ok"})))))

(defn create-hash-for-resetting-password [id]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [user (get-by-id id {:transaction t-con})]
      (if-let [errors (cond-> nil
                        (empty? user) (append-system-error "User does not exist"))]
        {:errors (json/write-str errors)}
        (let [hash (model.util/build-random-str-alphabets-and-number 30)]
          (jdbc/update! t-con :user
                        {:password_reset (json/write-str
                                          {:hash hash
                                           :until (f/unparse model.util/time-format-yyyymmdd-hhmmss
                                                             (t/plus (t/now) (t/days 1)))})}
                        ["id = ?" id])
          {:hash hash})))))

(defn reset-password-for-hash-user [args]
  (jdbc/with-db-transaction [t-con db-spec]
    (let [hash-password (:hash args)
          password (:password args)
          id-user (:id args)
          user (get-by-id-and-hash-password-reset id-user hash-password {:transaction t-con})]
      (if-let [errors (cond-> (-> nil
                                  (check-errors-of-params-for-password args))
                        (empty? user) (append-system-error "User does not exist"))]
        {:errors (json/write-str errors)}
        (do
          (jdbc/update! t-con :user
                        {:hash (build-hash password (:salt user))
                         :password_reset nil}
                        ["id = ?" id-user])
          {:message (util.label/changed-password)})))))
