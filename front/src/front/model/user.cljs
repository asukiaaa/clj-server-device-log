(ns front.model.user
  (:require goog.string
            [front.model.util :refer [escape-str]]
            [re-graph.core :as re-graph]
            [front.model.util :as util]))

(def keys-for-user [:id :email :name :permission :created_at :updated_at])
(def str-keys-for-user (clojure.string/join " " (map name keys-for-user)))

(defn login [{:keys [email password on-receive]}]
  (let [mutation (goog.string.format "{ login(email: \"%s\", password: \"%s\") { id email name } }"
                                     (escape-str email) (escape-str password))]
    (re-graph/mutate mutation () (fn [{:keys [data]}]
                                   (on-receive (:login data))))))

(defn logout [{:keys [on-receive]}]
  (re-graph/mutate "{ logout }" () (fn [{:keys [data]}]
                                     (on-receive (:logout data)))))

(defn get-loggedin [{:keys [on-receive]}]
  (let [query (goog.string.format "{ user_loggedin { %s } }" str-keys-for-user)]
    (re-graph/query query () (fn [{:keys [data]}]
                               (on-receive (:user_loggedin data))))))

(defn fetch-list-and-total [{:keys [on-receive]}]
  (let [query (goog.string.format "{ users { total list { %s } } }" str-keys-for-user)]
    (println :query query)
    (re-graph/query query () (fn [{:keys [data]}]
                               (on-receive (:users data))))))

(defn fetch-by-id [{:keys [id on-receive]}]
  (let [query (goog.string.format "{ user(id: %d) { %s } }" (int id) str-keys-for-user)]
    (re-graph/query query () (fn [{:keys [data]}]
                               (on-receive (:user data))))))

(defn create [{:keys [name email password permission on-receive]}]
  (let [require-url-password-reset "false"
        query (goog.string.format "{ user(user: { name: \"%s\", email: \"%s\", password: \"%s\", permission: \"%s\" }, requireUrlPasswordReset: %s ) { errors url_password_reset user { %s } } }"
                                  (util/escape-str name)
                                  (util/escape-str email)
                                  (util/escape-str password)
                                  (util/escape-str permission)
                                  require-url-password-reset
                                  str-keys-for-user)]
    (re-graph/mutate query () (fn [{:keys [data]}]
                                (on-receive (:user data))))))
