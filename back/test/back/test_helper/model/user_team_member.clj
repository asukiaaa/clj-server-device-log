(ns back.test-helper.model.user-team-member
  (:require
   [clojure.spec.alpha :as s]
   [back.models.user-team-member :as model.user-team-member]
   [cheshire.core :as cheshire]))

(s/fdef with-member-params
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 2)
               :body (s/* any?)))

(defmacro with-member-params
  {:clj-kondo/lint-as 'clojure.core/let}
  [[user-team-member params] & body]
  `(let [~user-team-member (model.user-team-member/create ~params)]
     (try
       ~@body
       (finally
         (model.user-team-member/delete (:id ~user-team-member))))))

(s/fdef with-member
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 2)
               :body (s/* any?)))

(defmacro with-member
  {:clj-kondo/lint-as 'clojure.core/let}
  [[user-team-member [user-team user]] & body]
  `(with-member-params [~user-team-member
                        {:user_team_id (:id ~user-team)
                         :member_id (:id ~user)}]
     ~@body))

(s/fdef with-member-admin
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 2)
               :body (s/* any?)))

(defmacro with-member-admin
  {:clj-kondo/lint-as 'clojure.core/let}
  [[user-team-member [user-team user]] & body]
  `(with-member-params [~user-team-member
                        {:user_team_id (:id ~user-team)
                         :member_id (:id ~user)
                         :permission (cheshire/generate-string {:admin true})}]
     ~@body))
