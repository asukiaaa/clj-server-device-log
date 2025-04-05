(ns back.test-helper.model.bundled
  (:require
   [clojure.spec.alpha :as s]
   [back.test-helper.model.user :as h.user]
   [back.test-helper.model.user-team :as h.user-team]
   [back.test-helper.model.user-team-member :as h.user-team-member]))

(s/fdef with-data-user-no-team
  :args (s/cat :binding (s/coll-of any? :kind vector? :min-count 1 :max-count 2)
               :body (s/* any?)))

(defmacro with-data-user-no-team
  {:clj-kondo/lint-as 'clojure.core/let}
  [[data-for-test & [options]] & body]
  `(h.user/with-user-admin [user-admin#]
     (h.user/with-user [user-standalone#]
       (let [~data-for-test (-> (:data-to-merge ~options)
                                (assoc-in [:map-user :no-team :admin] user-admin#)
                                (assoc-in [:map-user :no-team :standalone] user-standalone#))]
         ~@body))))

(s/fdef with-data-user-and-team-for-test
  :args (s/cat :binding (s/coll-of any? :kind vector? :min-count 1 :max-count 2)
               :body (s/* any?)))

(defmacro with-data-user-and-team
  {:clj-kondo/lint-as 'clojure.core/let}
  [[data-for-test & [options]] & body]
  `(h.user/with-user [user-team-owner#]
     (h.user/with-user [user-team-member#]
       (h.user-team/with-user-team [user-team# user-team-owner#]
         (h.user-team-member/with-member [_member# [user-team# user-team-member#]]
           (h.user/with-user [user-team-member-admin#]
             (h.user-team-member/with-member-admin [_member-admin# [user-team# user-team-member-admin#]]
               (let [key-team# (or (:key-team ~options) :team)
                     ~data-for-test (-> (:data-to-merge ~options)
                                        (assoc-in [:map-user key-team#]
                                                  {:owner user-team-owner#
                                                   :member user-team-member#
                                                   :member-admin user-team-member-admin#})
                                        (assoc-in [:map-team key-team#] user-team#))]
                 ~@body))))))))
