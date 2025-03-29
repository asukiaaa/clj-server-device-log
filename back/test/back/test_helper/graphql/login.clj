(ns back.test-helper.graphql.login
  (:require
   [clj-http.cookies :as http.cookies]
   [clojure.spec.alpha :as s]
   [venia.core :as v]
   [back.test-helper.graphql.core :refer [post-query]]))

(defn build-query-get-user-loggedin []
  (v/graphql-query {:venia/queries [[:user_loggedin [[:user [:id :email]]]]]}))

(defn build-mutation-login [email password]
  (v/graphql-query
   {:venia/operation {:operation/type :mutation
                      :operation/name "Login"}
    :venia/queries [[:login {:email email
                             :password password}
                     [:id :email]]]}))

(defn get-user-loggedin [cookie-store]
  (let [query (build-query-get-user-loggedin)
        result (post-query query {:cookie-store cookie-store})]
    (-> result :body :data :user_loggedin :user)))

(s/fdef with-loggedin-session
  :args (s/cat :binding (s/coll-of any? :kind vector? :count 2)
               :body (s/* any?)))

(defmacro with-loggedin-session
  {:clj-kondo/lint-as 'clojure.core/let}
  [[cookie-store user] & body]
  `(let [~cookie-store (http.cookies/cookie-store)]
     (post-query (build-mutation-login (:email ~user) (:password ~user))
                 {:cookie-store ~cookie-store})
     ~@body))
