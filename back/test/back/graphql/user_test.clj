(ns back.graphql.user-test
  (:require
   [clojure.test :as t]
   [back.core]
   [back.test-helper.graphql.login :as graphql.login]
   [back.test-helper.graphql.user :as graphql.user]
   [back.test-helper.model.bundled :as h.model.bundled]))

(defn test-is-in-list [label item list]
  (t/testing label
    (t/is (some #(= (:id %) (:id item)) list))))

(defn test-is-not-in-list [label item list]
  (t/testing label
    (t/is (not (some #(= (:item %) (:id item)) list)))))

(defn test-visible [{:keys [key-watcher key-user user users cookie-store]}]
  (test-is-in-list (str key-watcher " can see " key-user " on list") user users)
  (let [user-by-query (graphql.user/get-user cookie-store (:id user))]
    (t/testing (str key-watcher " can see " key-user " by get query")
      (t/is (not (nil? user-by-query)))
      (t/is (= (:email user-by-query) (:email user))))))

(defn test-invisible [{:keys [key-watcher key-user user users cookie-store]}]
  (t/testing (str "invisible test " key-watcher " cannot see " key-user " should handle with not nil element")
    (t/is (not (nil? (:id user)))))
  (test-is-not-in-list (str key-watcher " cannot see " key-user " on list") user users)
  (let [user-by-query (graphql.user/get-user cookie-store (:id user))]
    (t/testing (str key-watcher " cannot see " key-user " by get query")
      (t/is (nil? user-by-query)))))

(defn test-user-visivility [{:keys [map-user]}]
  (t/is (seq map-user))
  (let [keys-user-in-team [:team-owner :team-member :team-member-admin]
        cases-test
        [{:key-watcher :admin
          :keys-user-visible (keys map-user)}
         {:key-watcher :team-owner
          :keys-user-visible keys-user-in-team}
         {:key-watcher :team-member
          :keys-user-visible keys-user-in-team}
         {:key-watcher :team-member-admin
          :keys-user-visible keys-user-in-team}
         {:key-watcher :out-of-team
          :keys-user-visible [:out-of-team]}]]
    (doseq [{:keys [key-watcher keys-user-visible]} cases-test]
      (graphql.login/with-loggedin-session [cookie-store (key-watcher map-user)]
        (let [list-and-total-user (graphql.user/get-users cookie-store)
              users (:list list-and-total-user)]
          (doseq [key-user (keys map-user)]
            (let [user (key-user map-user)
                  info-test
                  {:key-watcher key-watcher
                   :key-user key-user
                   :user user
                   :users users
                   :cookie-store cookie-store}]
              (if (some #(= % key-user) keys-user-visible)
                (test-visible info-test)
                (test-invisible info-test)))))))))

(t/deftest user
  (h.model.bundled/with-data-user-and-team [data]
    (test-user-visivility data)))
