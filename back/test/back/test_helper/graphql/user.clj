(ns back.test-helper.graphql.user
  (:require
   [venia.core :as v]
   [back.test-helper.graphql.core :refer [post-query]]))

(defn build-query-get-users []
  (v/graphql-query {:venia/queries [[:users {:page 0 :limit 100}
                                     [[:list [:id :name :email]] :total]]]}))

(defn build-query-get-user [id]
  (v/graphql-query {:venia/queries [[:user {:id id}
                                     [:id :name :email]]]}))

(defn get-users [cookie-store]
  (let [query (build-query-get-users)
        result (post-query query {:cookie-store cookie-store})]
    (-> result :body :data :users)))

(defn get-user [cookie-store id]
  (let [query (build-query-get-user id)
        result (post-query query {:cookie-store cookie-store})]
    (-> result :body :data :user)))
