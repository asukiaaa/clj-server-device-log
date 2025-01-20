(ns front.model.user
  (:refer-clojure :exclude [update])
  (:require [goog.string :refer [format]]
            clojure.string
            [clojure.walk :refer [keywordize-keys]]
            [front.model.util :as util :refer [build-input-str-for-str]]
            [re-graph.core :as re-graph]))

(def keys-for-user [:id :email :name :permission :created_at :updated_at])
(def str-keys-for-user (clojure.string/join " " (map name keys-for-user)))

(defn build-select-options-from-list-and-total [list-and-total]
  (for [item (:list list-and-total)]
    (let [id (:id item)]
      [id (str id " " (:name item) " " (:email item))])))

(defn admin? [user]
  (->> user :permission (.parse js/JSON) js->clj keywordize-keys :role (= "admin")))

(defn login [{:keys [email password on-receive]}]
  (let [mutation (goog.string.format "{ login(email: \"%s\", password: \"%s\") { id email name } }"
                                     (build-input-str-for-str email) (build-input-str-for-str password))]
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
                              :str-keys-of-item str-keys-for-user
                              :on-receive on-receive
                              :limit limit
                              :page page}))

(defn fetch-by-id [{:keys [id on-receive]}]
  (util/fetch-by-id {:name-table "user"
                     :str-keys-of-item str-keys-for-user
                     :id id
                     :on-receive on-receive}))

(defn fetch-by-id-and-password-reset-hash [{:keys [id hash on-receive str-keys]}]
  (util/fetch-by-id {:name-table "user_for_resetting_password"
                     :str-keys-of-item (or str-keys "id") #_str-keys-for-user
                     :str-additional-params (str "hash: " (util/build-input-str-for-str hash))
                     :id id
                     :on-receive on-receive}))

(defn create [{:keys [name email password permission on-receive]}]
  (let [require-url-password-reset "false"]
    (util/create {:name-table "user"
                  :str-input-params
                  (format "user: { name: \"%s\", email: \"%s\", password: \"%s\", permission: \"%s\" }, requireUrlPasswordReset: %s"
                          (util/build-input-str-for-str name)
                          (util/build-input-str-for-str email)
                          (util/build-input-str-for-str password)
                          (util/build-input-str-for-str permission)
                          require-url-password-reset)
                  :str-keys-receive (format "url_password_reset user { %s }"
                                            str-keys-for-user)
                  :on-receive on-receive})))

(defn update [{:keys [id name email permission on-receive]}]
  (let [query (format "{ user_update(id: %d, user: { name: \"%s\", email: \"%s\", permission: \"%s\" }) { errors user { %s } } }"
                      (util/build-input-str-for-int id)
                      (util/build-input-str-for-str name)
                      (util/build-input-str-for-str email)
                      (util/build-input-str-for-str permission)
                      str-keys-for-user)]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (:user_update data) (util/build-error-messages errors))))))

(defn build-str-user [user]
  (str "user id:" (:id user) " name:" (:name user)))

(defn build-confirmation-message-for-deleting [user]
  (str "delete " (build-str-user user)))

(defn build-confirmation-message-for-creating-hash-password-reset [user]
  (str "create hash to reset password of" (build-str-user user)))

(defn delete [{:keys [id on-receive]}]
  (util/delete-by-id {:name-table "user"
                      :id id
                      :on-receive on-receive}))

(defn reset-password-mine [{:keys [password password-new on-receive]}]
  (util/mutate-with-receive-params
   {:str-field "password_mine_reset"
    :str-input-params (format "password: %s, password_new: %s"
                              (util/build-input-str-for-str password)
                              (util/build-input-str-for-str password-new))
    :str-keys-receive "message"
    :on-receive on-receive}))

(defn create-hash-to-reset-password [{:keys [id-user on-receive]}]
  (let [key-field :hash_for_resetting_password_create
        query (format "{ %s (id: %s) { errors hash } }"
                      (name key-field)
                      (util/build-input-str-for-int id-user))]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (key-field data) (util/build-error-messages errors))))))

(defn password-reset-by-id-and-hash [{:keys [id hash password on-receive]}]
  (let [key-field :password_for_hash_user_reset
        query (format "{ %s (id: %s, hash: %s, password: %s) { errors message } }"
                      (name key-field)
                      (util/build-input-str-for-int id)
                      (util/build-input-str-for-str hash)
                      (util/build-input-str-for-str password))]
    (re-graph/mutate query () (fn [{:keys [data errors]}]
                                (on-receive (key-field data) (util/build-error-messages errors))))))
