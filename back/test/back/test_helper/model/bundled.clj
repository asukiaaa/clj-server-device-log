(ns back.test-helper.model.bundled
  (:require
   [clojure.spec.alpha :as s]
   [back.test-helper.model.user :as h.user]
   [back.test-helper.model.user-team :as h.user-team]
   [back.test-helper.model.user-team-member :as h.user-team-member]))


(s/fdef with-data-user-and-team-for-test
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 1)
               :body (s/* any?)))

(defmacro with-data-user-and-team
  {:clj-kondo/lint-as 'clojure.core/fn}
  [[data-for-test] & body]
  `(h.user/with-user-admin [user-admin#]
     (h.user/with-user [user-team-owner#]
       (h.user/with-user [user-team-member#]
         (h.user-team/with-user-team [user-team# user-team-owner#]
           (h.user-team-member/with-member [_member# [user-team# user-team-member#]]
             (h.user/with-user [user-team-member-admin#]
               (h.user-team-member/with-member-admin [_member-admin# [user-team# user-team-member-admin#]]
                 (h.user/with-user [user-out-of-team#]
                   (let [~data-for-test {:map-user {:admin user-admin#
                                                    :team-owner user-team-owner#
                                                    :team-member user-team-member#
                                                    :team-member-admin user-team-member-admin#
                                                    :out-of-team user-out-of-team#}
                                         :map-team {:team user-team#}}]
                     ~@body))))))))))
