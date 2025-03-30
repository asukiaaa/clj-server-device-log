(ns back.graphql.user-test
  (:require
   [clojure.test :as t]
   [back.core]
   [back.test-helper.user :as h.user]
   [back.test-helper.user-team :as h.user-team]
   [back.test-helper.user-team-member :as h.user-team-member]
   [back.test-helper.graphql.login :as graphql.login]
   [back.test-helper.graphql.user :as graphql.user]))

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

(defn test-user-visivility [{:keys [user-admin user-owner-of-team user-team-member user-team-member-admin user-out-of-team]
                             :as map-user}]
  (let [keys-user-in-team [:user-owner-of-team :user-team-member :user-team-member-admin]
        cases-test
        [{:watcher user-admin
          :key-watcher :user-admin
          :keys-user-visible (keys map-user)}
         {:watcher user-owner-of-team
          :key-watcher :user-owner-of-team
          :keys-user-visible keys-user-in-team}
         {:watcher user-team-member
          :key-watcher :user-team-member
          :keys-user-visible keys-user-in-team}
         {:watcher user-team-member-admin
          :key-watcher :user-team-member-admin
          :keys-user-visible keys-user-in-team}
         {:watcher user-out-of-team
          :key-watcher :user-out-of-team
          :keys-user-visible [:user-out-of-team]}]]
    (doseq [{:keys [watcher key-watcher keys-user-visible]} cases-test]
      (graphql.login/with-loggedin-session [cookie-store watcher]
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
  (h.user/with-user-admin [user-admin]
    (h.user/with-user [user-owner-of-team]
      (h.user/with-user [user-team-member]
        (h.user-team/with-user-team [user-team user-owner-of-team]
          (h.user-team-member/with-member [_member [user-team user-team-member]]
            (h.user/with-user [user-team-member-admin]
              (h.user-team-member/with-member-admin [_member-admin [user-team user-team-member-admin]]
                (h.user/with-user [user-out-of-team]
                  (let [map-user {:user-admin user-admin
                                  ;:user-fail {:id 0 :email "aaa"}
                                  :user-owner-of-team user-owner-of-team
                                  :user-team-member user-team-member
                                  :user-team-member-admin user-team-member-admin
                                  :user-out-of-team user-out-of-team}]
                    (test-user-visivility map-user)))))))))))
