(ns front.model.user
  (:refer-clojure :exclude [update])
  (:require goog.string
            clojure.string
            [clojure.walk :refer [keywordize-keys]]
            [front.model.util :as util :refer [escape-str]]
            [re-graph.core :as re-graph]))

(def keys-for-user [:id :email :name :permission :created_at :updated_at])
(def str-keys-for-user (clojure.string/join " " (map name keys-for-user)))

(defn admin? [user]
  (->> user :permission (.parse js/JSON) js->clj keywordize-keys :role (= "admin")))

(defn login [{:keys [email password on-receive]}]
  (let [mutation (goog.string.format "{ login(email: \"%s\", password: \"%s\") { id email name } }"
                                     (escape-str email) (escape-str password))]
    (re-graph/mutate mutation () (fn [{:keys [data errors]}]
                                   (on-receive (:login data) (util/build-error-messages errors))))))

(defn logout [{:keys [on-receive]}]
  (re-graph/mutate "{ logout }" () (fn [{:keys [data errors]}]
                                     (on-receive (:logout data) (util/build-error-messages errors)))))

(defn get-loggedin [{:keys [on-receive]}]
  (let [query (goog.string.format "{ user_loggedin { %s } }" str-keys-for-user)]
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (on-receive (:user_loggedin data) (util/build-error-messages errors))))))

(defn fetch-list-and-total [{:keys [on-receive]}]
  (let [query (goog.string.format "{ users { total list { %s } } }" str-keys-for-user)]
    (println :query query)
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (on-receive (:users data) (util/build-error-messages errors))))))

(defn fetch-by-id [{:keys [id on-receive]}]
  (let [query (goog.string.format "{ user(id: %d) { %s } }" (int id) str-keys-for-user)]
    (re-graph/query query () (fn [{:keys [data errors]}]
                               (on-receive (:user data) (util/build-error-messages errors))))))

(defn create [{:keys [name email password permission on-receive]}]
  (let [require-url-password-reset "false"
        query (goog.string.format "{ userCreate(user: { name: \"%s\", email: \"%s\", password: \"%s\", permission: \"%s\" }, requireUrlPasswordReset: %s ) { errors url_password_reset user { %s } } }"
                                  (util/escape-str name)
                                  (util/escape-str email)
                                  (util/escape-str password)
                                  (util/escape-str permission)
                                  require-url-password-reset
                                  str-keys-for-user)]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (:userCreate data) (util/build-error-messages errors))))))

(defn update [{:keys [id name email permission on-receive]}]
  (println id name email permission)
  (let [query (goog.string.format "{ userEdit(id: %d, user: { name: \"%s\", email: \"%s\", permission: \"%s\" }) { errors user { %s } } }"
                                  (util/escape-str id)
                                  (util/escape-str name)
                                  (util/escape-str email)
                                  (util/escape-str permission)
                                  str-keys-for-user)]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (println data)
                                (on-receive (:userEdit data) (util/build-error-messages errors))))))
