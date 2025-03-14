(ns back.core-test
  (:require [clojure.core :refer [format]]
            [clojure.test :as t]
            [back.core]
            [back.test-helper.user :as h.user]
            [re-graph.core :as re-graph]
            [venia.core :as v]
            [back.config :refer [port]]))

(re-graph/init {:http {:url (format "http://localhost:%d/graphql" port)
                       :supported-operations #{:query :mutate}}
                :ws nil})

(t/deftest test-login
  (let [user (h.user/create-admin-user)]
    (t/testing "Able to create admin user"
      #_(println user)
      (t/is (not (nil? (:id user)))))
    (t/testing "Empty user before login"
      (let [query (v/graphql-query {:venia/queries [[:user_loggedin [[:user [:id]]]]]})]
        #_(println :query query)
        (re-graph/query
         {:query query
          :callback
          (fn [{:keys [data errors]}]
            (println :data data :errors errors) ; not shown
            (t/is (not (nil? (:user data)))))})))
    (t/testing "Able to login"
      (let [query (v/graphql-query {:venia/queries [[:login {:email (:email user)
                                                             :password (:password user)}
                                                     [[:id :email]]]]})]
        (re-graph/mutate
         {:query query
          :callback
          (fn [{:keys [data errors]}]
            (println :data data :errors errors) ; not shown
            (t/is (and (nil? errors) (= (:email data) (:email user)))))})))))

(t/deftest a-test
  (t/testing "FIXME, I fail."
    (t/is (= 0 1))))

#_(t/run-tests)

#_(defn main [& args]
    (t/run-tests)
    (println "hi on main in test"))
