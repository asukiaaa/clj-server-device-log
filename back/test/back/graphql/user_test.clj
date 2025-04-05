(ns back.graphql.user-test
  (:require
   [clojure.test :as t]
   [back.core]
   [back.test-helper.graphql.login :as graphql.login]
   [back.test-helper.graphql.user :as graphql.user]
   [back.test-helper.graphql.util :as graphql.util]
   [back.test-helper.model.bundled :as h.model.bundled]
   [back.test-helper.model.user :as h.model.user]))

(defn test-user-visivility [{:keys [map-user]}]
  (t/is (seq map-user))
  (let [keys-user-in-team [[:team :owner] [:team :member] [:team :member-admin]]
        cases-test
        [{:key-watcher [:no-team :admin]
          :keys-user-visible (reduce (fn [keys-user key-team]
                                       (concat keys-user (for [key-user (keys (key-team map-user))]
                                                           [key-team key-user])))
                                     [] (keys map-user))}
         {:key-watcher [:team :owner]
          :keys-user-visible keys-user-in-team}
         {:key-watcher [:team :member]
          :keys-user-visible keys-user-in-team}
         {:key-watcher [:team :member-admin]
          :keys-user-visible keys-user-in-team}
         {:key-watcher [:no-team :standalone]
          :keys-user-visible [[:no-team :standalone]]}]]
    (doseq [{:keys [key-watcher keys-user-visible]} cases-test]
      (let [user-watcher (get-in map-user key-watcher map-user)]
        (t/is (seq user-watcher))
        (graphql.login/with-loggedin-session [cookie-store user-watcher]
          (let [list-and-total-user (graphql.user/get-list-and-total cookie-store)
                users (:list list-and-total-user)]
            (doseq [key-team (keys map-user)]
              (doseq [key-user (keys (key-team map-user))]
                (let [key-user [key-team key-user]
                      user (get-in map-user key-user)
                      info-test
                      {:key-watcher key-watcher
                       :key-item key-user
                       :item user
                       :list users
                       :get-item-by-id #(graphql.user/get-by-id cookie-store %)}]
                  (if (some #(= % key-user) keys-user-visible)
                    (graphql.util/test-visible info-test)
                    (graphql.util/test-invisible info-test)))))))))))

(t/deftest core
  (h.model.bundled/with-data-user-no-team [data nil]
    (h.model.bundled/with-data-user-and-team [data {:data-to-merge data}]
      (test-user-visivility data))))
