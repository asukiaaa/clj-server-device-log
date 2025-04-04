(ns back.test-helper.model.user-team
  (:require
   [clojure.spec.alpha :as s]
   [back.models.user-team :as model.user-team]))

(defn build-params-for-owner [user-owner]
  {:owner_user_id (:id user-owner)
   :name (str "team-for-" (:name user-owner))})

(s/fdef with-user-team
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 2)
               :body (s/* any?)))

(defmacro with-user-team
  {:clj-kondo/lint-as 'clojure.core/let}
  [[user-team user-owner] & body]
  `(let [~user-team (model.user-team/create (build-params-for-owner ~user-owner))]
     (try
       ~@body
       (finally
         (model.user-team/delete (:id ~user-team))))))
