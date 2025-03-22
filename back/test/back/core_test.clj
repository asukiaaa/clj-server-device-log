(ns back.core-test
  (:require [cheshire.core :as cheshire]
            [clojure.core :refer [format]]
            [clojure.test :as t]
            [back.core]
            [back.test-helper.user :as h.user]
            [clj-http.client :as http-client]
            [venia.core :as v]
            [back.config :refer [port]]))

(defn build-url []
  (format "http://localhost:%d/graphql" port))

(defn post-query [query]
  (let [body (cheshire/generate-string {:query query})]
    #_(println :body-request body)
    (-> (http-client/post (build-url)
                          {:body body
                           :content-type :json
                           :throw-exceptions? false})
        ((fn [result]
           (assoc result :body (cheshire/parse-string (:body result) true))))
        #_((fn [item] (println :body-response (:body item)) item)))))

(defn build-query-get-user-loggedin []
  (v/graphql-query {:venia/queries [[:user_loggedin [[:user [:id :email]]]]]}))

(defn build-mutation-login [email password]
  (v/graphql-query
   {:venia/operation {:operation/type :mutation
                      :operation/name "Login"}
    :venia/queries [[:login {:email email
                             :password password}
                     [:id :email]]]}))

(defn get-id-user-loggedin [data]
  (-> :data :user_loggedin :user :id))

(t/deftest test-login
  (let [user (h.user/create-admin-user)]
    (t/testing "Able to create admin user"
      #_(println user)
      (t/is (not (nil? (:id user)))))
    (t/testing "Empty user before login"
      (let [query (build-query-get-user-loggedin)
            {:keys [body]} (post-query query)]
        #_(println :query query)
        #_(println :result body)
        (t/is (nil? (get-id-user-loggedin (:data body))))))
    (t/testing "Able to login"
      (let [query (build-mutation-login (:email user) (:password user))
            {:keys [body]} (post-query query)]
        #_(println :query query)
        #_(println :result body)
        (t/is (not (nil? (-> body :data :login :id))))))))

(def tests-to-create
  ["TODO list for create test for graphql"
   "users: admin can see all users"
   "users: not admin can see users connected via user team"
   "device_logs_for_watch_scope: cannot see logs that the drvice does not belongs to user team by member"
   "files_for_watch_scope: cannot see logs that the drvice does not belongs to user team by member"])

#_(t/run-tests)

#_(defn main [& args]
    (t/run-tests)
    (println "hi on main in test"))
