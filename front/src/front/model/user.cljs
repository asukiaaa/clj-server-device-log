(ns front.model.user
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
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

(defn fetch-list-and-total [{:keys [on-receive limit page]}]
  (util/fetch-list-and-total {:name-table "users"
                              :str-keys-of-list str-keys-for-user
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table "user"
                     :str-keys-of-list str-keys-for-user
                     :id id
                     :on-receive on-receive}))

(defn create [{:keys [name email password permission on-receive]}]
  (let [require-url-password-reset "false"]
    (util/create {:name-table "user"
                  :str-input-params
                  (format "user: { name: \"%s\", email: \"%s\", password: \"%s\", permission: \"%s\" }, requireUrlPasswordReset: %s"
                          (util/escape-str name)
                          (util/escape-str email)
                          (util/escape-str password)
                          (util/escape-str permission)
                          require-url-password-reset)
                  :str-keys-receive (format "url_password_reset user { %s }"
                                            str-keys-for-user)
                  :on-receive on-receive})))

(defn update [{:keys [id name email permission on-receive]}]
  (let [query (goog.string.format "{ user_update(id: %d, user: { name: \"%s\", email: \"%s\", permission: \"%s\" }) { errors user { %s } } }"
                                  (util/escape-int id)
                                  (util/escape-str name)
                                  (util/escape-str email)
                                  (util/escape-str permission)
                                  str-keys-for-user)]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                #_(println data)
                                (on-receive (:user_update data) (util/build-error-messages errors))))))

(defn build-confirmation-message-for-deleting [user]
  (str "delete user id:" (:id user) " name:" (:name user)))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table "user"
                      :id id
                      :on-receive on-receive}))
